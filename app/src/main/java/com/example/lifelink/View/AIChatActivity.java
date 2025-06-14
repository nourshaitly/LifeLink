package com.example.lifelink.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.RecognizerIntent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.ChatMessage;
import com.example.lifelink.Model.ChatSession;
import com.example.lifelink.Model.GeminiRequest;
import com.example.lifelink.Model.GeminiResponse;
import com.example.lifelink.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIChatActivity extends AppCompatActivity {

    private String sessionId, sessionTitle;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editText;
    private FirebaseFirestore db;
    private String uid;
    private GeminiAPI api;

    private DrawerLayout drawerLayout;
    private RecyclerView historyRecyclerView;
    private ChatHistoryAdapter historyAdapter;
    private List<ChatSession> chatSessions = new ArrayList<>();
    private TextToSpeech tts;

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        ImageButton voiceButton = findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(v -> startVoiceInput());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab_sos); // Make sure to have this FAB in your layout
        fab.setOnClickListener(v -> {
            DashboardUtils.triggerCall(this);
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView = findViewById(R.id.chatRecyclerView);
        editText = findViewById(R.id.messageEditText);
        api = ApiClient.getClient();

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        sessionId = getIntent().getStringExtra("sessionId");
        sessionTitle = getIntent().getStringExtra("sessionTitle");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size);

        adapter = new ChatAdapter(this, messages, sessionId, uid);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyAdapter = new ChatHistoryAdapter(chatSessions, session -> {
            Intent intent = new Intent(AIChatActivity.this, AIChatActivity.class);
            intent.putExtra("sessionId", session.getId());
            intent.putExtra("sessionTitle", session.getTitle());
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
            finish();
        });

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);

        if (sessionId != null) {
            getSupportActionBar().setTitle(sessionTitle);
            loadMessages();
        }

        findViewById(R.id.sendButton).setOnClickListener(v -> {
            String userMessage = editText.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                sendMessage(userMessage);
                editText.setText("");
            }
        });

        loadChatSessions();

        adapter.setOnMessageActionListener(new ChatAdapter.OnMessageActionListener() {
            @Override
            public void onCopy(ChatMessage message) {
                Context context = AIChatActivity.this;
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Message", message.message);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Message copied!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEdit(ChatMessage message, int position) {
                adapter.enableEditMode(position);
            }

            @Override
            public void onSpeak(ChatMessage message) {
                if (tts != null) {
                    tts.stop();
                    tts.shutdown();
                }
                tts = new TextToSpeech(AIChatActivity.this, status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.speak(message.message, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
            }

            @Override
            public void onSendEditedMessage(ChatMessage message, String newText) {
                sendMessage(newText);
            }
        });
    }

    private void ensureSessionCreated(String userMessage) {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();

            // ✅ Shorten title to max 40 characters
            String title = userMessage.length() > 40 ? userMessage.substring(0, 40) + "..." : userMessage;

            sessionTitle = title;

            ChatSession newSession = new ChatSession(sessionId, sessionTitle, System.currentTimeMillis());

            db.collection("users").document(uid).collection("ai_chats").document(sessionId)
                    .set(newSession)
                    .addOnSuccessListener(unused -> {
                        getSupportActionBar().setTitle(sessionTitle);
                        loadChatSessions();
                    });
        }
    }


    private void loadMessages() {
        db.collection("users").document(uid)
                .collection("ai_chats").document(sessionId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(snapshot -> {
                    messages.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        messages.add(message);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messages.size() - 1);
                });
    }

    private void sendMessage(String userMessage) {

        ensureSessionCreated(userMessage);

        // Create and show user message
        long userTimestamp = System.currentTimeMillis();
        ChatMessage userChatMessage = new ChatMessage(sessionId, userMessage, true, getTime(), userTimestamp);
        messages.add(userChatMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);

        // Save user message to Firestore
        db.collection("users").document(uid)
                .collection("ai_chats").document(sessionId)
                .collection("messages")
                .document(String.valueOf(userTimestamp))
                .set(userChatMessage);

        // Show typing indicator
        ChatMessage typingMessage = new ChatMessage(sessionId, "AI is typing...", false, getTime(), System.currentTimeMillis());
        messages.add(typingMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);

        // Send to Gemini API
        String prompt = "You are a medical assistant. Only answer medical-related questions. "
                + "If the question is not medical, say 'I can only answer medical questions.'\n\nUser: " + userMessage;

        GeminiRequest request = new GeminiRequest(prompt);
        api.sendPrompt(request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                messages.remove(typingMessage);
                adapter.notifyItemRemoved(messages.size());

                if (response.isSuccessful() && response.body() != null) {
                    String rawAnswer = response.body().candidates[0].content.parts[0].text;

                    if (rawAnswer.contains("I can only answer medical questions")) {
                        addMessage("AI refused to answer. This question is not medical.", false);
                        return;
                    }

                    // ✅ Add disclaimer before the AI's actual reply
                    String aiAnswer = "⚠️ You should consult a doctor for professional advice.\n\n" + rawAnswer;

                    // Save and show AI response
                    long aiTimestamp = System.currentTimeMillis();
                    ChatMessage aiChatMessage = new ChatMessage(sessionId, aiAnswer, false, getTime(), aiTimestamp);
                    messages.add(aiChatMessage);
                    adapter.notifyItemInserted(messages.size() - 1);
                    recyclerView.scrollToPosition(messages.size() - 1);

                    db.collection("users").document(uid)
                            .collection("ai_chats").document(sessionId)
                            .collection("messages")
                            .document(String.valueOf(aiTimestamp))
                            .set(aiChatMessage);
                }
                else {
                    addMessage("Error: " + response.code(), false);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                messages.remove(typingMessage);
                adapter.notifyItemRemoved(messages.size());
                addMessage("Error: " + t.getMessage(), false);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0) {
                String spokenText = result.get(0);
                editText.setText(spokenText);
                editText.setSelection(spokenText.length());
            }
        }
    }

    private void addMessage(String text, boolean isUser) {
        long ts = System.currentTimeMillis();
        ChatMessage message = new ChatMessage(sessionId, text, isUser, getTime(), ts);
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private String getTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadChatSessions() {
        db.collection("users").document(uid)
                .collection("ai_chats")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    chatSessions.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        ChatSession session = doc.toObject(ChatSession.class);
                        if (session != null) chatSessions.add(session);
                    }
                    historyAdapter.notifyDataSetChanged();
                });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Your device does not support speech input", Toast.LENGTH_SHORT).show();
        }
    }
}
