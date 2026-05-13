package com.sliit.registration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StudentProfile Model - Member 2 (Weerasekara G.W.D.S)
 * Stores extended academic profile data separate from auth credentials.
 * 
 * OOP: Implements FilePersistable (Abstraction via Interface).
 * Encapsulation: All fields are private, accessed only via getters/setters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile implements FilePersistable {
    private String studentId;       // Links to User.userId
    private String fullName;
    private String email;
    private String phone;
    private String program;         // e.g. "Software Engineering"
    private boolean active;

    @Override
    public String toFileString() {
        return studentId + "|" + fullName + "|" + email + "|" + phone + "|" + program + "|" + active;
    }

    @Override
    public void fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            this.studentId = parts[0];
            this.fullName = parts[1];
            this.email = parts[2];
            this.phone = parts[3];
            this.program = parts[4];
            this.active = Boolean.parseBoolean(parts[5]);
        }
    }
}
