package com.example.lifelink.View;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelink.Model.ChatMessage;
import com.example.lifelink.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<ChatMessage> messages;
    private String sessionId;
    private String firebaseUserId;

    // Action Listener
    public interface OnMessageActionListener {
        void onCopy(ChatMessage message);
        void onEdit(ChatMessage message, int position);
        void onSpeak(ChatMessage message);
        void onSendEditedMessage(ChatMessage message, String newText);

    }

    private OnMessageActionListener actionListener;

    public void setOnMessageActionListener(OnMessageActionListener listener) {
        this.actionListener = listener;
    }

    public ChatAdapter(Context context, List<ChatMessage> messages, String sessionId, String uid) {
        this.context = context;
        this.messages = messages;
        this.sessionId = sessionId;
        this.firebaseUserId = uid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String msgText = message.message != null ? message.message : "";
        String msgTime = formatTime(message.timestamp);

        holder.messageTextView.setText(msgText);
        holder.timeTextView.setText(msgTime);

        LinearLayout.LayoutParams bubbleParams = (LinearLayout.LayoutParams) holder.bubbleContainer.getLayoutParams();

        if (message.isUser) {
            holder.bubbleContainer.setBackgroundResource(R.drawable.chat_bubble_user);
            holder.messageTextView.setTextColor(Color.WHITE);
            bubbleParams.gravity = Gravity.END;
            holder.actionAndTimeContainer.setGravity(Gravity.END);
        } else {
            holder.bubbleContainer.setBackgroundResource(R.drawable.chat_bubble_ai);
            holder.messageTextView.setTextColor(Color.BLACK);
            bubbleParams.gravity = Gravity.START;
            holder.actionAndTimeContainer.setGravity(Gravity.START);
        }

        holder.bubbleContainer.setLayoutParams(bubbleParams);

        // EDIT MODE
        if (message.isEditing) {
            holder.messageTextView.setVisibility(View.GONE);
            holder.messageEditText.setVisibility(View.VISIBLE);
            holder.sendEditIcon.setVisibility(View.VISIBLE);
            holder.messageEditText.setText(message.message);

            holder.sendEditIcon.setOnClickListener(v -> {
                String newText = holder.messageEditText.getText().toString().trim();
                if (!newText.isEmpty()) {
                    message.message = newText;
                    message.isEditing = false;

                    FirebaseFirestore.getInstance().collection("users")
                            .document(firebaseUserId)
                            .collection("ai_chats").document(sessionId)
                            .collection("messages")
                            .document(String.valueOf(message.timestamp))
                            .set(message, SetOptions.merge());

                    notifyItemChanged(holder.getAdapterPosition());

                    if (actionListener != null) {
                        actionListener.onSendEditedMessage(message, newText);
                    }
                }
            });

        } else {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageEditText.setVisibility(View.GONE);
            holder.sendEditIcon.setVisibility(View.GONE);
        }

        // ANIMATION
        holder.itemView.setAlpha(0f);
        holder.itemView.animate().alpha(1f).setDuration(400).start();

        // ICONS
        holder.copyIcon.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onCopy(message);
        });

        holder.editIcon.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEdit(message, position);
        });

        holder.speakerIcon.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onSpeak(message);
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout bubbleContainer, actionAndTimeContainer, actionIconsContainer;
        TextView messageTextView, timeTextView;
        EditText messageEditText;
        ImageView copyIcon, editIcon, speakerIcon, sendEditIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bubbleContainer = itemView.findViewById(R.id.bubbleContainer);
            actionAndTimeContainer = itemView.findViewById(R.id.actionAndTimeContainer);
            actionIconsContainer = itemView.findViewById(R.id.actionIconsContainer);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageEditText = itemView.findViewById(R.id.messageEditText);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            speakerIcon = itemView.findViewById(R.id.speakIcon);
            copyIcon = itemView.findViewById(R.id.copyIcon);
            editIcon = itemView.findViewById(R.id.editIcon);
            sendEditIcon = itemView.findViewById(R.id.sendEditIcon);
        }
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(timestamp));
    }

    // Edit mode enable method
    public void enableEditMode(int position) {
        if (position >= 0 && position < messages.size()) {
            ChatMessage message = messages.get(position);
            message.isEditing = true;
            notifyItemChanged(position);
        }
    }
}
