package com.sliit.registration.service.interfaces;

import com.sliit.registration.dto.CourseModuleDto;
import com.sliit.registration.dto.UserRegistrationDto;
import com.sliit.registration.model.CourseModule;
import com.sliit.registration.model.User;

import java.util.List;
import java.util.Optional;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║                IAdminService — Admin Operations Contract                 ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * ABSTRACTION (Interface):
 *   Consolidates ALL admin-facing business logic into a single contract:
 *     - Curriculum & Module Management (Member 5 — Jayathilaka W.M.U.S)
 *     - Security & User Management (Member 6 — Malaviarachchi M.U.T)
 *
 * POLYMORPHISM:
 *   registerUser() accepts UserRegistrationDto and uses the 'role' field
 *   to instantiate the correct User subclass at runtime.
 *
 * INFORMATION HIDING:
 *   The interface hides:
 *     - UUID generation strategy for user/module IDs.
 *     - DTO → Entity conversion logic.
 *     - File-based persistence implementation.
 */
public interface IAdminService {

    // ═══════════════════════════════════════════════════════════════════════
    //  MODULE MANAGEMENT (Member 5 — CRUD)
    // ═══════════════════════════════════════════════════════════════════════

    /** CREATE: Add a new course module from validated DTO */
    CourseModule addModule(CourseModuleDto dto);

    /** READ: Get all modules */
    List<CourseModule> getAllModules();

    /** READ: Get a specific module by ID */
    Optional<CourseModule> getModuleById(String moduleId);

    /** UPDATE: Update module capacity */
    boolean updateModuleCapacity(String moduleId, int newCapacity);

    /** DELETE: Remove a module */
    boolean removeModule(String moduleId);

    // ═══════════════════════════════════════════════════════════════════════
    //  USER MANAGEMENT (Member 6 — CRUD)
    // ═══════════════════════════════════════════════════════════════════════

    /** CREATE: Register a new user (Moderator or Admin) from validated DTO */
    User registerUser(UserRegistrationDto dto);

    /** READ: Get all system users */
    List<User> getAllUsers();

    /** READ: Get a specific user by ID */
    Optional<User> getUserById(String userId);

    /** UPDATE: Update a user's password */
    boolean updatePassword(String userId, String newPassword);

    /** DELETE: Delete a user account */
    boolean deleteUser(String userId);
}
