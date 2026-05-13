package com.sliit.registration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║              COURSE MODULE DTO (Data Transfer Object)                    ║
 * ║  Validates module creation/update input from the admin web form.         ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. ENCAPSULATION: Private fields with controlled getters/setters.
 *
 * 2. INFORMATION HIDING: The 'currentEnrollment' counter is NOT exposed
 *    in this DTO — it is managed internally by the Service/Repository layer.
 *    The admin can only set the module code, name, and capacity.
 *
 * VALIDATION ANNOTATIONS:
 * ─────────────────────────────────────────────────────────────────────────────
 * @NotBlank — Rejects null, empty, and whitespace-only strings.
 * @Pattern  — Enforces a regex: module codes must be uppercase letters + digits.
 * @Size     — Min/max character length for the module name.
 * @Min      — Capacity must be at least 1 seat.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseModuleDto {

    @NotBlank(message = "Module code is required")
    @Pattern(regexp = "^[A-Z]{2}\\d{4}$",
             message = "Module code must be 2 uppercase letters + 4 digits (e.g. SE2010)")
    private String moduleId;

    @NotBlank(message = "Module name is required")
    @Size(min = 3, max = 100, message = "Module name must be 3–100 characters")
    private String moduleName;

    @Min(value = 1, message = "Maximum capacity must be at least 1")
    private int maxCapacity;
}
