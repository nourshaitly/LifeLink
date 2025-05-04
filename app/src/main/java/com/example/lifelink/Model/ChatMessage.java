package com.example.lifelink.Model;

public class ChatMessage {
    public String sessionId;
    public String message;
    public boolean isUser;
    public String time;
    public long timestamp;

    public boolean isEditing = false; // 🔥 ADD THIS

    public ChatMessage() {}

    public ChatMessage(String sessionId, String message, boolean isUser, String time, long timestamp) {
        this.sessionId = sessionId;
        this.message = message;
        this.isUser = isUser;
        this.time = time;
        this.timestamp = timestamp;
    }
}
