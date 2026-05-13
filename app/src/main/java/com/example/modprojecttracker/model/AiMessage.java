package com.example.modprojecttracker.model;

public class AiMessage {

    public static final int ROLE_USER = 0;
    public static final int ROLE_ASSISTANT = 1;

    private final int role;
    private final String content;
    private final long createdAt;

    public AiMessage(int role, String content) {
        this(role, content, System.currentTimeMillis());
    }

    public AiMessage(int role, String content, long createdAt) {
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

