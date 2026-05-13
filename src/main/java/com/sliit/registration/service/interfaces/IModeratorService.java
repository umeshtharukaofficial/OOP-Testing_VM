package com.sliit.registration.service.interfaces;

import com.sliit.registration.model.CourseModule;
import com.sliit.registration.model.EnrollmentRequest;
import com.sliit.registration.model.WaitlistEntry;

import java.util.List;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║             IModeratorService — Moderator Operations Contract            ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * ABSTRACTION (Interface):
 *   Consolidates ALL moderator-facing business logic into a single contract:
 *     - Waitlist Management with FIFO Queue (Member 3 — Kallora K.M.T.D)
 *     - Request Auditing with Insertion Sort (Member 4 — Arachchi V.A.K.S.V)
 *
 * INFORMATION HIDING:
 *   The interface hides:
 *     - The FIFO re-indexing algorithm used after waitlist removal.
 *     - The Insertion Sort algorithm used for request chronological ordering.
 *     - Module capacity checks during approval.
 */
public interface IModeratorService {

    // ═══════════════════════════════════════════════════════════════════════
    //  WAITLIST OPERATIONS (Member 3 — FIFO Queue CRUD)
    // ═══════════════════════════════════════════════════════════════════════

    /** CREATE: Add student to override waitlist (FIFO) */
    WaitlistEntry addToWaitlist(String studentId, String moduleCode);

    /** READ: View all waitlist entries */
    List<WaitlistEntry> getAllWaitlistEntries();

    /** READ: View waitlist for a specific module */
    List<WaitlistEntry> getWaitlistForModule(String moduleCode);

    /** READ: Get all modules (for dropdown population) */
    List<CourseModule> getAllModules();

    /** UPDATE: Modify queue position */
    boolean updateWaitlistPosition(String waitlistId, int newPosition);

    /** DELETE: Remove student from waitlist (with FIFO re-indexing) */
    boolean removeFromWaitlist(String waitlistId);

    // ═══════════════════════════════════════════════════════════════════════
    //  AUDIT OPERATIONS (Member 4 — Insertion Sort CRUD)
    // ═══════════════════════════════════════════════════════════════════════

    /** CREATE: Generate batch approval list (sorted via Insertion Sort) */
    List<EnrollmentRequest> generateBatchList();

    /** READ: View all requests sorted chronologically */
    List<EnrollmentRequest> getAllRequestsSorted();

    /** UPDATE: Approve a request (with module capacity check) */
    boolean approveRequest(String requestId);

    /** UPDATE: Reject a request */
    boolean rejectRequest(String requestId);

    /** DELETE: Clear all processed (APPROVED/REJECTED) requests */
    void clearProcessedRequests();
}
