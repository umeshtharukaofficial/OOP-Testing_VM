package com.sliit.registration.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Student extends User {
    private double gpa;
    private int enrolledCredits;

    public Student(String userId, String username, String passwordHash, double gpa, int enrolledCredits) {
        super(userId, username, passwordHash, "STUDENT");
        this.gpa = gpa;
        this.enrolledCredits = enrolledCredits;
    }

    @Override
    public String getDashboardUrl() {
        return "/student-home";
    }

    @Override
    public String toFileString() {
        return getUserId() + "|" + getUsername() + "|" + getPasswordHash() + "|" + getRole() + "|" + gpa + "|" + enrolledCredits;
    }

    @Override
    public void fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 6) {
            setUserId(parts[0]);
            setUsername(parts[1]);
            setPasswordHash(parts[2]);
            setRole(parts[3]);
            this.gpa = Double.parseDouble(parts[4]);
            this.enrolledCredits = Integer.parseInt(parts[5]);
        }
    }
}
