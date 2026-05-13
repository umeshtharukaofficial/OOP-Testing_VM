package com.sliit.registration.service;

import com.sliit.registration.model.CourseModule;
import com.sliit.registration.model.EnrollmentRequest;
import com.sliit.registration.model.StudentProfile;
import com.sliit.registration.repository.CourseModuleRepository;
import com.sliit.registration.repository.EnrollmentRequestRepository;
import com.sliit.registration.repository.StudentProfileRepository;
import com.sliit.registration.service.interfaces.IStudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║            StudentServiceImpl — implements IStudentService               ║
 * ║  Consolidates Member 1 (Enrollment) + Member 2 (Profile) logic.         ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. ABSTRACTION: Implements IStudentService — controllers depend on the
 *    interface, never on this concrete class.
 *
 * 2. ENCAPSULATION: Three private final repository dependencies are hidden.
 *    All business rules (capacity check, FIFO ordering) are encapsulated here.
 *
 * 3. INFORMATION HIDING:
 *    - Module capacity check before enrollment → hidden from Controller.
 *    - UUID generation strategy → hidden from Controller.
 *    - Soft-delete (deactivation) logic → hidden from Controller.
 *
 * CONSTRUCTOR INJECTION:
 * ─────────────────────────────────────────────────────────────────────────────
 * All 3 repositories injected via single constructor. No @Autowired annotation.
 */
@Slf4j
@Service
public class StudentServiceImpl implements IStudentService {

    private final EnrollmentRequestRepository requestRepository;
    private final CourseModuleRepository moduleRepository;
    private final StudentProfileRepository profileRepository;

    // ── Constructor Injection — all dependencies explicit and immutable ──
    public StudentServiceImpl(EnrollmentRequestRepository requestRepository,
                               CourseModuleRepository moduleRepository,
                               StudentProfileRepository profileRepository) {
        this.requestRepository = requestRepository;
        this.moduleRepository = moduleRepository;
        this.profileRepository = profileRepository;
        log.info("StudentServiceImpl initialized via constructor injection");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ENROLLMENT OPERATIONS (Member 1 — Umer D.R.S)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     *
     * BUSINESS RULE: Checks module capacity before allowing enrollment.
     * If the module is full, the request is still created with status "WAITLISTED"
     * instead of "PENDING" — a key enhancement over the previous implementation.
     */
    @Override
    public EnrollmentRequest submitEnrollmentRequest(String studentId, String moduleCode) {
        log.info("submitEnrollmentRequest() — student: '{}', module: '{}'", studentId, moduleCode);

        // ── Business Rule: Check module capacity ──
        String status = "PENDING";
        Optional<CourseModule> moduleOpt = moduleRepository.findById(moduleCode);
        if (moduleOpt.isPresent()) {
            CourseModule module = moduleOpt.get();
            if (module.getCurrentEnrollment() >= module.getMaxCapacity()) {
                log.warn("submitEnrollmentRequest() — Module '{}' is FULL ({}/{}). Request will be WAITLISTED.",
                        moduleCode, module.getCurrentEnrollment(), module.getMaxCapacity());
                status = "WAITLISTED";
            }
        }

        String requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        EnrollmentRequest request = new EnrollmentRequest(
                requestId, studentId, moduleCode, status, System.currentTimeMillis()
        );
        requestRepository.save(request);

        log.info("submitEnrollmentRequest() — Request '{}' created with status: '{}'", requestId, status);
        return request;
    }

    /** {@inheritDoc} */
    @Override
    public List<EnrollmentRequest> getStudentRequests(String studentId) {
        log.info("getStudentRequests() — Fetching requests for student: '{}'", studentId);
        return requestRepository.findByStudentId(studentId);
    }

    /** {@inheritDoc} */
    @Override
    public List<CourseModule> getAvailableModules() {
        log.info("getAvailableModules() — Fetching all modules for enrollment form");
        return moduleRepository.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public boolean swapModule(String requestId, String newModuleCode) {
        log.info("swapModule() — Swapping request '{}' to module '{}'", requestId, newModuleCode);
        Optional<EnrollmentRequest> opt = requestRepository.findById(requestId);
        if (opt.isPresent() && "PENDING".equals(opt.get().getStatus())) {
            EnrollmentRequest req = opt.get();
            req.setModuleCode(newModuleCode);
            req.setTimestamp(System.currentTimeMillis());
            requestRepository.save(req);
            log.info("swapModule() — Request '{}' swapped successfully", requestId);
            return true;
        }
        log.warn("swapModule() — Cannot swap: request '{}' is not PENDING or not found", requestId);
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean withdrawRequest(String requestId) {
        log.info("withdrawRequest() — Withdrawing request: '{}'", requestId);
        Optional<EnrollmentRequest> opt = requestRepository.findById(requestId);
        if (opt.isPresent() && "PENDING".equals(opt.get().getStatus())) {
            requestRepository.deleteById(requestId);
            log.info("withdrawRequest() — Request '{}' withdrawn", requestId);
            return true;
        }
        log.warn("withdrawRequest() — Cannot withdraw: request '{}' is not PENDING or not found", requestId);
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PROFILE OPERATIONS (Member 2 — Weerasekara G.W.D.S)
    // ═══════════════════════════════════════════════════════════════════════

    /** {@inheritDoc} */
    @Override
    public StudentProfile createProfile(String studentId, String fullName, String email,
                                         String phone, String program) {
        log.info("createProfile() — Creating profile for student: '{}'", studentId);

        // Check for existing profile (idempotent operation)
        Optional<StudentProfile> existing = profileRepository.findByStudentId(studentId);
        if (existing.isPresent()) {
            log.info("createProfile() — Profile already exists for '{}', returning existing", studentId);
            return existing.get();
        }

        StudentProfile profile = new StudentProfile(studentId, fullName, email, phone, program, true);
        profileRepository.save(profile);
        log.info("createProfile() — Profile created for student: '{}'", studentId);
        return profile;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<StudentProfile> getProfile(String studentId) {
        log.info("getProfile() — Fetching profile for student: '{}'", studentId);
        return profileRepository.findByStudentId(studentId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateContactInfo(String studentId, String email, String phone) {
        log.info("updateContactInfo() — Updating contact for student: '{}'", studentId);
        Optional<StudentProfile> opt = profileRepository.findByStudentId(studentId);
        if (opt.isPresent()) {
            StudentProfile profile = opt.get();
            profile.setEmail(email);
            profile.setPhone(phone);
            profileRepository.save(profile);
            log.info("updateContactInfo() — Contact updated for student: '{}'", studentId);
            return true;
        }
        log.warn("updateContactInfo() — Profile not found for student: '{}'", studentId);
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deactivateProfile(String studentId) {
        log.info("deactivateProfile() — Deactivating profile for student: '{}'", studentId);
        Optional<StudentProfile> opt = profileRepository.findByStudentId(studentId);
        if (opt.isPresent()) {
            StudentProfile profile = opt.get();
            profile.setActive(false);
            profileRepository.save(profile);
            log.info("deactivateProfile() — Profile deactivated for student: '{}'", studentId);
            return true;
        }
        log.warn("deactivateProfile() — Profile not found for student: '{}'", studentId);
        return false;
    }
}
