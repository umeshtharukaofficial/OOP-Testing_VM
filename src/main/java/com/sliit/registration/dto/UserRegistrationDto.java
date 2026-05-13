package com.sliit.registration.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║              USER REGISTRATION DTO (Data Transfer Object)                ║
 * ║  Validates admin/moderator registration input from the web form.         ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. ENCAPSULATION: All fields are private. Validation constraints are
 *    declared as metadata annotations — the Controller layer never manually
 *    checks string lengths or null values.
 *
 * 2. ABSTRACTION: This single DTO handles BOTH Moderator and Admin
 *    registration. The Service layer inspects the 'role' field to decide
 *    which concrete User subclass to instantiate (Polymorphism).
 *
 * 3. INFORMATION HIDING: Fields like userId and passwordHash are NOT present
 *    here — they are generated server-side by the Service layer. The form
 *    cannot inject or manipulate internal entity identifiers.
 *
 * VALIDATION ANNOTATIONS:
 * ─────────────────────────────────────────────────────────────────────────────
 * @NotBlank  — Rejects null, empty "", and whitespace-only "   " strings.
 * @Size      — Enforces min/max character length constraints.
 * @Min/@Max  — Enforces numeric range constraints for clearanceLevel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100, message = "Password must be at least 4 characters")
    private String password;

    @NotBlank(message = "Role must be specified")
    private String role;  // "MODERATOR" or "ADMIN"

    // ── Moderator-specific field ──
    @Size(max = 50, message = "Department ID cannot exceed 50 characters")
    private String departmentId;

    // ── Admin-specific field ──
    @Min(value = 1, message = "Clearance level must be at least 1")
    @Max(value = 5, message = "Clearance level cannot exceed 5")
    private int clearanceLevel;
}
