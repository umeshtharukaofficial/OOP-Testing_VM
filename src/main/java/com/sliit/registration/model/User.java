package com.sliit.registration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base abstract class demonstrating Inheritance, Abstraction, and Encapsulation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class User implements FilePersistable {
    private String userId;
    private String username;
    private String passwordHash;
    private String role; // STUDENT, MODERATOR, ADMIN

    // Polymorphism: abstract method to be implemented by child classes
    public abstract String getDashboardUrl();
}
