package com.sliit.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║                     LOGIN DTO (Data Transfer Object)                     ║
 * ║  Decouples the login form from the internal User entity model.           ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * WHY DTOs?
 * ─────────────────────────────────────────────────────────────────────────────
 * In industry-standard applications, we NEVER expose core entity models
 * directly to the web/controller layer. DTOs act as a firewall:
 *   • They validate input BEFORE it reaches the Service layer.
 *   • They prevent mass-assignment attacks (only declared fields are bound).
 *   • They decouple the API contract from the persistence model.
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. ENCAPSULATION: Private fields with controlled access via getters/setters.
 * 2. INFORMATION HIDING: The Controller never sees the raw User entity fields
 *    like userId, role, or passwordHash — only what the form needs.
 *
 * VALIDATION:
 * ─────────────────────────────────────────────────────────────────────────────
 * Uses Jakarta Bean Validation (jakarta.validation.constraints) annotations.
 * When paired with @Valid in the Controller, Spring auto-validates before
 * the request even reaches the Service layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 3, max = 100, message = "Password must be between 3 and 100 characters")
    private String password;
}
