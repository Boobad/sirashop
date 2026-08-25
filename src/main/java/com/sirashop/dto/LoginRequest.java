package com.sirashop.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String username;
    private String password;

    public String getIdentifier() {
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        return null;
    }
}
