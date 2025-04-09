package com.cloudbalance.dto;

public class LoginResponse {
    private String message;
    private String role;
    private String token;
    private String email;

    public LoginResponse(String message, String role, String token, String email) {
        this.message = message;
        this.role = role;
        this.token = token;
        this.email = email;
    }

    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
