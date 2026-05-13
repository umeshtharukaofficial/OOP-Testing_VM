package com.sliit.registration.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Moderator extends User {
    private String departmentId;

    public Moderator(String userId, String username, String passwordHash, String departmentId) {
        super(userId, username, passwordHash, "MODERATOR");
        this.departmentId = departmentId;
    }

    @Override
    public String getDashboardUrl() {
        return "/moderator-home";
    }

    @Override
    public String toFileString() {
        return getUserId() + "|" + getUsername() + "|" + getPasswordHash() + "|" + getRole() + "|" + departmentId;
    }

    @Override
    public void fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            setUserId(parts[0]);
            setUsername(parts[1]);
            setPasswordHash(parts[2]);
            setRole(parts[3]);
            this.departmentId = parts[4];
        }
    }
}
