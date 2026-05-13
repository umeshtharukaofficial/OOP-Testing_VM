package com.sliit.registration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WaitlistEntry Model - Member 3 (Kallora K.M.T.D)
 * Represents a student waiting in a FIFO override queue for a full module.
 * 
 * OOP: Implements FilePersistable (Abstraction via Interface).
 * Encapsulation: All fields private.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistEntry implements FilePersistable {
    private String waitlistId;
    private String studentId;
    private String moduleCode;
    private int queuePosition;      // FIFO position

    @Override
    public String toFileString() {
        return waitlistId + "|" + studentId + "|" + moduleCode + "|" + queuePosition;
    }

    @Override
    public void fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 4) {
            this.waitlistId = parts[0];
            this.studentId = parts[1];
            this.moduleCode = parts[2];
            this.queuePosition = Integer.parseInt(parts[3]);
        }
    }
}
