package com.sliit.registration.service;

import com.sliit.registration.model.CourseModule;
import com.sliit.registration.model.EnrollmentRequest;
import com.sliit.registration.model.WaitlistEntry;
import com.sliit.registration.repository.CourseModuleRepository;
import com.sliit.registration.repository.EnrollmentRequestRepository;
import com.sliit.registration.repository.WaitlistRepository;
import com.sliit.registration.service.interfaces.IModeratorService;
import com.sliit.registration.util.SortingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ModeratorServiceImpl — implements IModeratorService.
 * Consolidates Member 3 (Waitlist FIFO) + Member 4 (Audit Insertion Sort).
 *
 * OOP: Abstraction (interface), Encapsulation (private repos),
 * Information Hiding (FIFO re-indexing, capacity check hidden from controller).
 * Constructor Injection — no @Autowired fields.
 */
@Slf4j
@Service
public class ModeratorServiceImpl implements IModeratorService {

    private final WaitlistRepository waitlistRepository;
    private final EnrollmentRequestRepository requestRepository;
    private final CourseModuleRepository moduleRepository;

    public ModeratorServiceImpl(WaitlistRepository waitlistRepository,
                                 EnrollmentRequestRepository requestRepository,
                                 CourseModuleRepository moduleRepository) {
        this.waitlistRepository = waitlistRepository;
        this.requestRepository = requestRepository;
        this.moduleRepository = moduleRepository;
        log.info("ModeratorServiceImpl initialized via constructor injection");
    }

    // ─── WAITLIST OPERATIONS (Member 3) ───

    @Override
    public WaitlistEntry addToWaitlist(String studentId, String moduleCode) {
        log.info("addToWaitlist() — student: '{}', module: '{}'", studentId, moduleCode);
        int nextPos = waitlistRepository.getNextPosition(moduleCode);
        String id = "WL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        WaitlistEntry entry = new WaitlistEntry(id, studentId, moduleCode, nextPos);
        waitlistRepository.save(entry);
        log.info("addToWaitlist() — Created at position {}", nextPos);
        return entry;
    }

    @Override
    public List<WaitlistEntry> getAllWaitlistEntries() {
        return waitlistRepository.findAll();
    }

    @Override
    public List<WaitlistEntry> getWaitlistForModule(String moduleCode) {
        return waitlistRepository.findByModuleCode(moduleCode);
    }

    @Override
    public List<CourseModule> getAllModules() {
        return moduleRepository.findAll();
    }

    @Override
    public boolean updateWaitlistPosition(String waitlistId, int newPosition) {
        log.info("updateWaitlistPosition() — id: '{}', pos: {}", waitlistId, newPosition);
        Optional<WaitlistEntry> opt = waitlistRepository.findById(waitlistId);
        if (opt.isPresent()) {
            opt.get().setQueuePosition(newPosition);
            waitlistRepository.save(opt.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeFromWaitlist(String waitlistId) {
        log.info("removeFromWaitlist() — id: '{}'", waitlistId);
        Optional<WaitlistEntry> opt = waitlistRepository.findById(waitlistId);
        if (opt.isPresent()) {
            String moduleCode = opt.get().getModuleCode();
            waitlistRepository.deleteById(waitlistId);
            // FIFO re-indexing
            List<WaitlistEntry> remaining = waitlistRepository.findByModuleCode(moduleCode);
            for (int i = 0; i < remaining.size(); i++) {
                remaining.get(i).setQueuePosition(i + 1);
                waitlistRepository.save(remaining.get(i));
            }
            log.info("removeFromWaitlist() — Removed, {} re-indexed", remaining.size());
            return true;
        }
        return false;
    }

    // ─── AUDIT OPERATIONS (Member 4) ───

    @Override
    public List<EnrollmentRequest> generateBatchList() {
        List<EnrollmentRequest> pending = requestRepository.findByStatus("PENDING");
        SortingUtils.insertionSortRequests(pending);
        return pending;
    }

    @Override
    public List<EnrollmentRequest> getAllRequestsSorted() {
        List<EnrollmentRequest> all = requestRepository.findAll();
        SortingUtils.insertionSortRequests(all);
        return all;
    }

    @Override
    public boolean approveRequest(String requestId) {
        log.info("approveRequest() — id: '{}'", requestId);
        Optional<EnrollmentRequest> opt = requestRepository.findById(requestId);
        if (opt.isPresent()) {
            EnrollmentRequest req = opt.get();
            Optional<CourseModule> moduleOpt = moduleRepository.findById(req.getModuleCode());
            if (moduleOpt.isPresent()) {
                CourseModule mod = moduleOpt.get();
                if (mod.getCurrentEnrollment() < mod.getMaxCapacity()) {
                    req.setStatus("APPROVED");
                    requestRepository.save(req);
                    mod.setCurrentEnrollment(mod.getCurrentEnrollment() + 1);
                    moduleRepository.save(mod);
                    log.info("approveRequest() — APPROVED ({}/{})", mod.getCurrentEnrollment(), mod.getMaxCapacity());
                    return true;
                }
                log.warn("approveRequest() — Module FULL");
            }
        }
        return false;
    }

    @Override
    public boolean rejectRequest(String requestId) {
        log.info("rejectRequest() — id: '{}'", requestId);
        Optional<EnrollmentRequest> opt = requestRepository.findById(requestId);
        if (opt.isPresent()) {
            opt.get().setStatus("REJECTED");
            requestRepository.save(opt.get());
            return true;
        }
        return false;
    }

    @Override
    public void clearProcessedRequests() {
        log.info("clearProcessedRequests() — Clearing APPROVED and REJECTED");
        requestRepository.deleteByStatus("APPROVED");
        requestRepository.deleteByStatus("REJECTED");
    }
}
