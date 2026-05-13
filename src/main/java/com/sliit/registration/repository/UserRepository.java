package com.sliit.registration.repository;

import com.sliit.registration.exception.DataAccessException;
import com.sliit.registration.model.Admin;
import com.sliit.registration.model.Moderator;
import com.sliit.registration.model.Student;
import com.sliit.registration.model.User;
import com.sliit.registration.util.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║                    USER REPOSITORY (File-Based)                          ║
 * ║  Member 6 (Malaviarachchi M.U.T) — Security & User Management           ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. ENCAPSULATION:
 *    - FILE_PATH is private static final. No external class knows where user
 *      data is stored on disk.
 *    - The FileStorageService dependency is private and injected via constructor.
 *
 * 2. ABSTRACTION:
 *    - Exposes clean CRUD methods: findAll(), findById(), save(), delete().
 *    - Callers never deal with file parsing, line splitting, or lock acquisition.
 *
 * 3. POLYMORPHISM:
 *    - When reading from users.txt, the repository inspects the "role" field
 *      and dynamically instantiates the correct subclass (Student, Moderator,
 *      or Admin). The returned List<User> contains mixed concrete types that
 *      are all accessed through the abstract User reference.
 *
 * 4. INFORMATION HIDING:
 *    - IOException is NEVER exposed to the Service layer. All file failures
 *      are wrapped in DataAccessException by the FileStorageService.
 *    - The pipe-delimited format ("|") is an internal implementation detail.
 *
 * FILE HANDLING:
 * ─────────────────────────────────────────────────────────────────────────────
 * - Delegates ALL file I/O to FileStorageService (thread-safe with ReadWriteLock).
 * - Data format: ID|Username|Password|Role|ExtraData (pipe-delimited .txt)
 * - Uses BufferedReader/BufferedWriter internally via FileStorageService.
 *
 * SPRING MVC PATTERN:
 * ─────────────────────────────────────────────────────────────────────────────
 * - @Repository annotation marks this as the Data Access Layer.
 * - Injected into @Service classes (AuthService, UserManagementService).
 * - Controllers NEVER access this directly — strict layered architecture.
 */
@Slf4j
@Repository
public class UserRepository {

    // ── ENCAPSULATION: File path is private, hidden from all other layers ──
    private static final String FILE_PATH = "src/main/resources/database/users.txt";

    // ── Dependency Injection: Thread-safe file operations ──
    private final FileStorageService fileStorageService;

    @Autowired
    public UserRepository(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        log.info("UserRepository initialized — data file: {}", FILE_PATH);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  READ OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Reads ALL users from users.txt.
     *
     * POLYMORPHISM IN ACTION:
     * The method returns List<User> but the actual objects inside are
     * Student, Moderator, or Admin instances — determined at runtime by
     * inspecting the "role" field in each pipe-delimited line.
     *
     * @return List of all User objects (polymorphic — mixed concrete types).
     * @throws DataAccessException if file reading fails.
     */
    public List<User> findAll() {
        log.info("findAll() — Loading all users from: {}", FILE_PATH);

        try {
            List<String> lines = fileStorageService.readAllLines(FILE_PATH);
            List<User> users = new ArrayList<>();

            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    String role = parts[3];
                    User user = null;

                    // ── POLYMORPHISM: Dynamic subclass instantiation based on role ──
                    switch (role) {
                        case "STUDENT":
                            user = new Student();
                            break;
                        case "MODERATOR":
                            user = new Moderator();
                            break;
                        case "ADMIN":
                            user = new Admin();
                            break;
                        default:
                            log.warn("Unknown role '{}' encountered in line: {}", role, line);
                            continue;
                    }

                    user.fromFileString(line);
                    users.add(user);
                }
            }

            log.info("findAll() — Loaded {} users successfully", users.size());
            return users;

        } catch (DataAccessException e) {
            log.error("findAll() FAILED — {}", e.getMessage());
            throw e; // Re-throw — already a DataAccessException
        }
    }

    /**
     * Finds a single user by their unique ID.
     *
     * @param id The userId to search for.
     * @return Optional containing the User if found, or empty.
     */
    public Optional<User> findById(String id) {
        log.info("findById() — Searching for user: {}", id);
        Optional<User> result = findAll().stream()
                .filter(u -> u.getUserId().equals(id))
                .findFirst();

        if (result.isPresent()) {
            log.info("findById() — Found user: {} (role: {})", id, result.get().getRole());
        } else {
            log.warn("findById() — User not found: {}", id);
        }
        return result;
    }

    /**
     * Finds a single user by their username.
     *
     * @param username The username to search for.
     * @return Optional containing the User if found.
     */
    public Optional<User> findByUsername(String username) {
        log.info("findByUsername() — Searching for: {}", username);
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CREATE / UPDATE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Saves a User to the file. If a user with the same ID exists, it is updated.
     * If no matching ID is found, the user is appended (Created).
     *
     * POLYMORPHISM: The method accepts the abstract User type. Whether the
     * actual object is a Student, Moderator, or Admin, the correct
     * toFileString() override is called dynamically at runtime.
     *
     * @param user The User object to save (can be any subclass).
     * @throws DataAccessException if file writing fails.
     */
    public void save(User user) {
        log.info("save() — Saving user: {} (role: {})", user.getUserId(), user.getRole());

        try {
            List<User> users = findAll();
            boolean updated = false;

            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserId().equals(user.getUserId())) {
                    users.set(i, user);
                    updated = true;
                    log.info("save() — Updated existing user: {}", user.getUserId());
                    break;
                }
            }

            if (!updated) {
                users.add(user);
                log.info("save() — Created new user: {}", user.getUserId());
            }

            // ── POLYMORPHISM: toFileString() resolves to the correct subclass override ──
            List<String> lines = users.stream()
                    .map(User::toFileString)
                    .collect(Collectors.toList());

            fileStorageService.writeAllLines(FILE_PATH, lines);
            log.info("save() — User {} persisted successfully", user.getUserId());

        } catch (DataAccessException e) {
            log.error("save() FAILED for user {} — {}", user.getUserId(), e.getMessage());
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DELETE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Deletes a user by their unique ID.
     * Reads all users, removes the matching one, and writes the remainder back.
     *
     * @param id The userId of the user to delete.
     * @throws DataAccessException if file operations fail.
     */
    public void delete(String id) {
        log.info("delete() — Deleting user: {}", id);

        try {
            List<User> users = findAll();
            int originalSize = users.size();
            users.removeIf(u -> u.getUserId().equals(id));

            if (users.size() < originalSize) {
                List<String> lines = users.stream()
                        .map(User::toFileString)
                        .collect(Collectors.toList());

                fileStorageService.writeAllLines(FILE_PATH, lines);
                log.info("delete() — User {} removed successfully", id);
            } else {
                log.warn("delete() — User {} not found, nothing deleted", id);
            }

        } catch (DataAccessException e) {
            log.error("delete() FAILED for user {} — {}", id, e.getMessage());
            throw e;
        }
    }
}
