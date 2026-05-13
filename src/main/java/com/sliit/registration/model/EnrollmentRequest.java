package com.sliit.registration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EnrollmentRequest Model - Member 1 (Umer D.R.S) & Member 4 (Arachchi V.A.K.S.V)
 * Represents a student's course registration request.
 * 
 * OOP: Implements FilePersistable (Abstraction via Interface).
 * Encapsulation: All fields private, accessed via Lombok-generated getters/setters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest implements FilePersistable {
    private String requestId;
    private String studentId;
    private String moduleCode;
    private String status;          // PENDING, APPROVED, REJECTED
    private long timestamp;

    @Override
    public String toFileString() {
        return requestId + "|" + studentId + "|" + moduleCode + "|" + status + "|" + timestamp;
    }

    @Override
    public void fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            this.requestId = parts[0];
            this.studentId = parts[1];
            this.moduleCode = parts[2];
            this.status = parts[3];
            this.timestamp = Long.parseLong(parts[4]);
        }
    }
}
