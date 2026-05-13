package com.sliit.registration.service.interfaces;

import com.sliit.registration.model.CourseModule;
import com.sliit.registration.model.EnrollmentRequest;
import com.sliit.registration.model.StudentProfile;

import java.util.List;
import java.util.Optional;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║               IStudentService — Student Operations Contract              ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * ABSTRACTION (Interface):
 *   Consolidates ALL student-facing business logic into a single contract:
 *     - Enrollment Processing (Member 1 — Umer D.R.S)
 *     - Academic Profile Management (Member 2 — Weerasekara G.W.D.S)
 *
 *   Controllers inject IStudentService, not the implementation class.
 *
 * INFORMATION HIDING:
 *   The interface hides all internal details:
 *     - Which repositories are used.
 *     - How module capacity is checked before enrollment.
 *     - The file format for storing profile data.
 */
public interface IStudentService {

    // ═══════════════════════════════════════════════════════════════════════
    //  ENROLLMENT OPERATIONS (Member 1 — CRUD)
    // ═══════════════════════════════════════════════════════════════════════

    /** CREATE: Submit a new enrollment request */
    EnrollmentRequest submitEnrollmentRequest(String studentId, String moduleCode);

    /** READ: View all enrollment requests for a student */
    List<EnrollmentRequest> getStudentRequests(String studentId);

    /** READ: Get all available modules for enrollment form */
    List<CourseModule> getAvailableModules();

    /** UPDATE: Swap module in a pending request */
    boolean swapModule(String requestId, String newModuleCode);

    /** DELETE: Withdraw a pending enrollment request */
    boolean withdrawRequest(String requestId);

    // ═══════════════════════════════════════════════════════════════════════
    //  PROFILE OPERATIONS (Member 2 — CRUD)
    // ═══════════════════════════════════════════════════════════════════════

    /** CREATE: Initialize student profile on first login */
    StudentProfile createProfile(String studentId, String fullName, String email,
                                  String phone, String program);

    /** READ: Get student profile by ID */
    Optional<StudentProfile> getProfile(String studentId);

    /** UPDATE: Edit contact information */
    boolean updateContactInfo(String studentId, String email, String phone);

    /** DELETE: Deactivate (soft delete) student profile */
    boolean deactivateProfile(String studentId);
}
