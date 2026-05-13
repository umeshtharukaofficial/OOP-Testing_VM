package com.sliit.registration.repository;

import com.sliit.registration.exception.DataAccessException;
import com.sliit.registration.model.EnrollmentRequest;
import com.sliit.registration.util.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EnrollmentRequestRepository — File-based persistence for requests.txt
 * Member 1 (Umer D.R.S) & Member 4 (Arachchi V.A.K.S.V).
 *
 * OOP: Encapsulation (private file path), Abstraction (clean CRUD API).
 * File Handling: Delegates to thread-safe FileStorageService.
 */
@Slf4j
@Repository
public class EnrollmentRequestRepository {

    private static final String FILE_PATH = "src/main/resources/database/requests.txt";
    private final FileStorageService fileStorageService;

    @Autowired
    public EnrollmentRequestRepository(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        log.info("EnrollmentRequestRepository initialized — data file: {}", FILE_PATH);
    }

    /** READ: Returns all enrollment requests from file */
    public List<EnrollmentRequest> findAll() {
        log.info("findAll() — Loading requests from: {}", FILE_PATH);
        try {
            List<String> lines = fileStorageService.readAllLines(FILE_PATH);
            List<EnrollmentRequest> requests = new ArrayList<>();
            for (String line : lines) {
                EnrollmentRequest req = new EnrollmentRequest();
                req.fromFileString(line);
                requests.add(req);
            }
            log.info("findAll() — Loaded {} requests", requests.size());
            return requests;
        } catch (DataAccessException e) {
            log.error("findAll() FAILED — {}", e.getMessage());
            throw e;
        }
    }

    /** READ: Find a specific request by ID */
    public Optional<EnrollmentRequest> findById(String requestId) {
        return findAll().stream().filter(r -> r.getRequestId().equals(requestId)).findFirst();
    }

    /** READ: Find all requests for a specific student */
    public List<EnrollmentRequest> findByStudentId(String studentId) {
        return findAll().stream().filter(r -> r.getStudentId().equals(studentId)).collect(Collectors.toList());
    }

    /** READ: Find all requests with a specific status */
    public List<EnrollmentRequest> findByStatus(String status) {
        return findAll().stream().filter(r -> r.getStatus().equals(status)).collect(Collectors.toList());
    }

    /** CREATE or UPDATE: Save a request */
    public void save(EnrollmentRequest request) {
        log.info("save() — Saving request: {}", request.getRequestId());
        try {
            List<EnrollmentRequest> requests = findAll();
            boolean updated = false;
            for (int i = 0; i < requests.size(); i++) {
                if (requests.get(i).getRequestId().equals(request.getRequestId())) {
                    requests.set(i, request);
                    updated = true;
                    break;
                }
            }
            if (!updated) requests.add(request);

            List<String> lines = requests.stream().map(EnrollmentRequest::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
            log.info("save() — Request {} persisted successfully", request.getRequestId());
        } catch (DataAccessException e) {
            log.error("save() FAILED for request {} — {}", request.getRequestId(), e.getMessage());
            throw e;
        }
    }

    /** DELETE: Remove a request by ID */
    public void deleteById(String requestId) {
        log.info("deleteById() — Deleting request: {}", requestId);
        try {
            List<EnrollmentRequest> requests = findAll();
            requests.removeIf(r -> r.getRequestId().equals(requestId));
            List<String> lines = requests.stream().map(EnrollmentRequest::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
        } catch (DataAccessException e) {
            log.error("deleteById() FAILED — {}", e.getMessage());
            throw e;
        }
    }

    /** DELETE: Remove all requests matching a status */
    public void deleteByStatus(String status) {
        log.info("deleteByStatus() — Clearing all '{}' requests", status);
        try {
            List<EnrollmentRequest> requests = findAll();
            requests.removeIf(r -> r.getStatus().equals(status));
            List<String> lines = requests.stream().map(EnrollmentRequest::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
        } catch (DataAccessException e) {
            log.error("deleteByStatus() FAILED — {}", e.getMessage());
            throw e;
        }
    }
}
