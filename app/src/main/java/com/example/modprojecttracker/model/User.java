package com.example.modprojecttracker.model;

/** User entity for local authentication and ownership. */
public class User {

    public String uid;

    public String username;
    public String password;

    public String name;

    /** User role: admin or user. */
    public String role;

    public long createdAt;

    public User() {
    }

    public User(String uid, String username, String password, String name, String role, long createdAt) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /** Returns true when role is admin. */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}

