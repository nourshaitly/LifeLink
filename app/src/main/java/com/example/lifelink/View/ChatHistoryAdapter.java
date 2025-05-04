package com.example.lifelink.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.ChatSession;
import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {

    private List<ChatSession> sessions;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(ChatSession session);
    }

    public ChatHistoryAdapter(List<ChatSession> sessions, OnSessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatSession session = sessions.get(position);
        holder.titleTextView.setText(session.getTitle());

        // ✅ Format and set the time
        // ✅ Get timestamp from session
        long timestamp = session.getTimestamp();

// ✅ Format the timestamp
        String formattedTime = formatTimestamp(timestamp);

// ✅ Set formatted time text
        holder.timeTextView.setText(formattedTime);


        // Open chat on click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSessionClick(session);
        });

        // Delete chat on delete icon click
        holder.deleteIcon.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference sessionRef = db.collection("users").document(uid)
                        .collection("ai_chats").document(session.getId());

                // Delete all messages first
                sessionRef.collection("messages").get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }

                    // Delete session
                    sessionRef.delete().addOnSuccessListener(unused -> {
                        sessions.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(holder.itemView.getContext(), "Chat deleted", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    // ✅ Helper to format timestamp
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy - hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView timeTextView;
        ImageView deleteIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.sessionTitle);
            timeTextView = itemView.findViewById(R.id.sessionTime); // Make sure this exists in item_chat_history.xml
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }
}
