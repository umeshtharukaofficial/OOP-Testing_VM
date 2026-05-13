package com.sliit.registration.repository;

import com.sliit.registration.exception.DataAccessException;
import com.sliit.registration.model.StudentProfile;
import com.sliit.registration.util.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * StudentProfileRepository — File-based persistence for profiles.txt
 * Member 2 (Weerasekara G.W.D.S) — Academic Profile.
 *
 * OOP: Encapsulation (private file path), Abstraction (clean CRUD API).
 * File Handling: Delegates to thread-safe FileStorageService.
 */
@Slf4j
@Repository
public class StudentProfileRepository {

    private static final String FILE_PATH = "src/main/resources/database/profiles.txt";
    private final FileStorageService fileStorageService;

    @Autowired
    public StudentProfileRepository(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        log.info("StudentProfileRepository initialized — data file: {}", FILE_PATH);
    }

    /** READ: Return all student profiles */
    public List<StudentProfile> findAll() {
        log.info("findAll() — Loading profiles from: {}", FILE_PATH);
        try {
            List<String> lines = fileStorageService.readAllLines(FILE_PATH);
            List<StudentProfile> profiles = new ArrayList<>();
            for (String line : lines) {
                StudentProfile profile = new StudentProfile();
                profile.fromFileString(line);
                profiles.add(profile);
            }
            log.info("findAll() — Loaded {} profiles", profiles.size());
            return profiles;
        } catch (DataAccessException e) {
            log.error("findAll() FAILED — {}", e.getMessage());
            throw e;
        }
    }

    /** READ: Find profile by student ID */
    public Optional<StudentProfile> findByStudentId(String studentId) {
        return findAll().stream().filter(p -> p.getStudentId().equals(studentId)).findFirst();
    }

    /** CREATE or UPDATE: Save a profile */
    public void save(StudentProfile profile) {
        log.info("save() — Saving profile for student: {}", profile.getStudentId());
        try {
            List<StudentProfile> profiles = findAll();
            boolean updated = false;
            for (int i = 0; i < profiles.size(); i++) {
                if (profiles.get(i).getStudentId().equals(profile.getStudentId())) {
                    profiles.set(i, profile);
                    updated = true;
                    break;
                }
            }
            if (!updated) profiles.add(profile);

            List<String> lines = profiles.stream().map(StudentProfile::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
            log.info("save() — Profile for {} persisted", profile.getStudentId());
        } catch (DataAccessException e) {
            log.error("save() FAILED for profile {} — {}", profile.getStudentId(), e.getMessage());
            throw e;
        }
    }

    /** DELETE: Remove profile by student ID */
    public void deleteByStudentId(String studentId) {
        log.info("deleteByStudentId() — Deleting profile: {}", studentId);
        try {
            List<StudentProfile> profiles = findAll();
            profiles.removeIf(p -> p.getStudentId().equals(studentId));
            List<String> lines = profiles.stream().map(StudentProfile::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
        } catch (DataAccessException e) {
            log.error("deleteByStudentId() FAILED — {}", e.getMessage());
            throw e;
        }
    }
}
