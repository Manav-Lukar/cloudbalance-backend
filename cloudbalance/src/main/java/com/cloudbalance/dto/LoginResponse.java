package com.cloudbalance.dto;

public class LoginResponse {
    private String message;
    private String role;
    private String email;

    public LoginResponse(String message, String role, String email) {
        this.message = message;
        this.role = role;
        this.email = email;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
