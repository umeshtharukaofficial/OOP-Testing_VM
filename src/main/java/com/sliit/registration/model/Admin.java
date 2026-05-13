package com.sliit.registration.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Admin extends User {
    private int clearanceLevel;

    public Admin(String userId, String username, String passwordHash, int clearanceLevel) {
        super(userId, username, passwordHash, "ADMIN");
        this.clearanceLevel = clearanceLevel;
    }

    @Override
    public String getDashboardUrl() {
        return "/admin-home";
    }

    @Override
    public String toFileString() {
        return getUserId() + "|" + getUsername() + "|" + getPasswordHash() + "|" + getRole() + "|" + clearanceLevel;
    }

    @Override
    public void fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            setUserId(parts[0]);
            setUsername(parts[1]);
            setPasswordHash(parts[2]);
            setRole(parts[3]);
            this.clearanceLevel = Integer.parseInt(parts[4]);
        }
    }
}
