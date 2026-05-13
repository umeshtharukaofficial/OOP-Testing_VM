package com.sliit.registration.repository;

import com.sliit.registration.exception.DataAccessException;
import com.sliit.registration.model.WaitlistEntry;
import com.sliit.registration.util.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * WaitlistRepository — File-based persistence for waitlist.txt
 * Member 3 (Kallora K.M.T.D) — Waitlist Management (FIFO Queue).
 *
 * OOP: Encapsulation (private file path + FIFO logic), Abstraction (clean API).
 * File Handling: Delegates to thread-safe FileStorageService.
 */
@Slf4j
@Repository
public class WaitlistRepository {

    private static final String FILE_PATH = "src/main/resources/database/waitlist.txt";
    private final FileStorageService fileStorageService;

    @Autowired
    public WaitlistRepository(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        log.info("WaitlistRepository initialized — data file: {}", FILE_PATH);
    }

    /** READ: Return all waitlist entries */
    public List<WaitlistEntry> findAll() {
        log.info("findAll() — Loading waitlist from: {}", FILE_PATH);
        try {
            List<String> lines = fileStorageService.readAllLines(FILE_PATH);
            List<WaitlistEntry> entries = new ArrayList<>();
            for (String line : lines) {
                WaitlistEntry entry = new WaitlistEntry();
                entry.fromFileString(line);
                entries.add(entry);
            }
            log.info("findAll() — Loaded {} waitlist entries", entries.size());
            return entries;
        } catch (DataAccessException e) {
            log.error("findAll() FAILED — {}", e.getMessage());
            throw e;
        }
    }

    /** READ: Find entry by ID */
    public Optional<WaitlistEntry> findById(String waitlistId) {
        return findAll().stream().filter(e -> e.getWaitlistId().equals(waitlistId)).findFirst();
    }

    /** READ: Find all entries for a specific module, sorted by position (FIFO) */
    public List<WaitlistEntry> findByModuleCode(String moduleCode) {
        return findAll().stream()
                .filter(e -> e.getModuleCode().equals(moduleCode))
                .sorted((a, b) -> Integer.compare(a.getQueuePosition(), b.getQueuePosition()))
                .collect(Collectors.toList());
    }

    /** CREATE or UPDATE: Save a waitlist entry */
    public void save(WaitlistEntry entry) {
        log.info("save() — Saving waitlist entry: {}", entry.getWaitlistId());
        try {
            List<WaitlistEntry> entries = findAll();
            boolean updated = false;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getWaitlistId().equals(entry.getWaitlistId())) {
                    entries.set(i, entry);
                    updated = true;
                    break;
                }
            }
            if (!updated) entries.add(entry);

            List<String> lines = entries.stream().map(WaitlistEntry::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
            log.info("save() — Waitlist entry {} persisted", entry.getWaitlistId());
        } catch (DataAccessException e) {
            log.error("save() FAILED for entry {} — {}", entry.getWaitlistId(), e.getMessage());
            throw e;
        }
    }

    /** DELETE: Remove waitlist entry by ID */
    public void deleteById(String waitlistId) {
        log.info("deleteById() — Deleting waitlist entry: {}", waitlistId);
        try {
            List<WaitlistEntry> entries = findAll();
            entries.removeIf(e -> e.getWaitlistId().equals(waitlistId));
            List<String> lines = entries.stream().map(WaitlistEntry::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
        } catch (DataAccessException e) {
            log.error("deleteById() FAILED — {}", e.getMessage());
            throw e;
        }
    }

    /** Gets the next queue position for a given module (FIFO) */
    public int getNextPosition(String moduleCode) {
        List<WaitlistEntry> moduleEntries = findByModuleCode(moduleCode);
        if (moduleEntries.isEmpty()) return 1;
        return moduleEntries.get(moduleEntries.size() - 1).getQueuePosition() + 1;
    }
}
