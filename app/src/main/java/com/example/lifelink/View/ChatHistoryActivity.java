package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.ChatSession;
import com.example.lifelink.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatHistoryAdapter adapter;
    private List<ChatSession> chatSessions = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat History");
        }

        recyclerView = findViewById(R.id.chatRecyclerView);

        adapter = new ChatHistoryAdapter(chatSessions, session -> {
            Intent intent = new Intent(ChatHistoryActivity.this, AIChatActivity.class);
            intent.putExtra("sessionId", session.getId());
            intent.putExtra("sessionTitle", session.getTitle());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadChatSessions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatSessions();
    }

    private void loadChatSessions() {
        db.collection("users").document(uid)
                .collection("ai_chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    chatSessions.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        ChatSession session = doc.toObject(ChatSession.class);
                        if (session != null) {
                            chatSessions.add(session);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            createNewChat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createNewChat() {
        String newSessionId = UUID.randomUUID().toString();
        String title = "New Chat";
        long timestamp = System.currentTimeMillis();

        ChatSession newSession = new ChatSession(newSessionId, title, timestamp);

        db.collection("users").document(uid)
                .collection("ai_chats").document(newSessionId)
                .set(newSession)
                .addOnSuccessListener(unused -> {
                    Intent intent = new Intent(ChatHistoryActivity.this, AIChatActivity.class);
                    intent.putExtra("sessionId", newSessionId);
                    intent.putExtra("sessionTitle", title);
                    startActivity(intent);
                });
    }
}
