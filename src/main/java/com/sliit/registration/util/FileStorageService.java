package com.sliit.registration.util;

import com.sliit.registration.exception.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║               THREAD-SAFE FILE STORAGE SERVICE                           ║
 * ║  Industry-grade generic utility for concurrent .txt file operations      ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. ABSTRACTION:
 *    - Exposes only high-level operations (readAllLines, writeAllLines, appendLine).
 *    - Callers never deal with Paths, Streams, or Lock acquisition directly.
 *
 * 2. ENCAPSULATION:
 *    - The ReentrantReadWriteLock, file path resolution, and directory creation
 *      logic are all PRIVATE. No external class can touch them.
 *
 * 3. INFORMATION HIDING:
 *    - IOException is caught internally and re-thrown as our custom
 *      DataAccessException. Upstream layers (Service, Controller) are completely
 *      shielded from java.io implementation details.
 *
 * FILE HANDLING STRATEGY:
 * ─────────────────────────────────────────────────────────────────────────────
 * - Uses java.util.concurrent.locks.ReentrantReadWriteLock:
 *     • Multiple threads CAN read simultaneously (shared read lock).
 *     • Only ONE thread can write at a time (exclusive write lock).
 *     • A write lock blocks all readers, preventing dirty reads.
 * - Uses BufferedReader / BufferedWriter for efficient line-by-line I/O.
 * - Auto-creates parent directories and the file itself if they don't exist.
 *
 * LOGGING:
 * ─────────────────────────────────────────────────────────────────────────────
 * - SLF4J via Lombok @Slf4j — logs every file access, success, and error.
 */
@Slf4j
@Service
public class FileStorageService {

    // ── Thread Safety: One lock PER file path for fine-grained concurrency ──
    // For this university project scope, a single global lock is sufficient.
    // In production, you'd use a ConcurrentHashMap<String, ReadWriteLock>.
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // ═══════════════════════════════════════════════════════════════════════
    //  PUBLIC API — READ OPERATIONS (Shared Read Lock)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Reads ALL lines from a text file in a thread-safe manner.
     * Multiple threads can read concurrently without blocking each other.
     *
     * @param filePath Relative or absolute path to the .txt file.
     * @return List of strings, one per line. Returns empty list if file doesn't exist.
     * @throws DataAccessException if an I/O error occurs during reading.
     */
    public List<String> readAllLines(String filePath) {
        lock.readLock().lock();
        log.info("READ LOCK acquired — reading file: {}", filePath);

        try {
            ensureFileExists(filePath);

            List<String> lines = new ArrayList<>();

            // Using BufferedReader for efficient line-by-line reading (File Handling requirement)
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lines.add(line);
                    }
                }
            }

            log.info("READ SUCCESS — {} lines read from: {}", lines.size(), filePath);
            return lines;

        } catch (IOException e) {
            log.error("READ FAILED — Error reading file: {} | Cause: {}", filePath, e.getMessage());
            throw new DataAccessException("Failed to read file: " + filePath, e);
        } finally {
            lock.readLock().unlock();
            log.debug("READ LOCK released — file: {}", filePath);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PUBLIC API — WRITE OPERATIONS (Exclusive Write Lock)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Writes a complete list of lines to a file, REPLACING all existing content.
     * This is an atomic overwrite — acquires an exclusive write lock that blocks
     * ALL other readers AND writers until complete.
     *
     * @param filePath Relative or absolute path to the .txt file.
     * @param lines    List of strings to write (one per line).
     * @throws DataAccessException if an I/O error occurs during writing.
     */
    public void writeAllLines(String filePath, List<String> lines) {
        lock.writeLock().lock();
        log.info("WRITE LOCK acquired — writing {} lines to: {}", lines.size(), filePath);

        try {
            ensureFileExists(filePath);

            // Using BufferedWriter for efficient line-by-line writing (File Handling requirement)
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            log.info("WRITE SUCCESS — {} lines written to: {}", lines.size(), filePath);

        } catch (IOException e) {
            log.error("WRITE FAILED — Error writing file: {} | Cause: {}", filePath, e.getMessage());
            throw new DataAccessException("Failed to write file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
            log.debug("WRITE LOCK released — file: {}", filePath);
        }
    }

    /**
     * Appends a single line to the END of a file without overwriting existing content.
     * Useful for audit logs and quick inserts.
     *
     * @param filePath Relative or absolute path to the .txt file.
     * @param line     The line to append.
     * @throws DataAccessException if an I/O error occurs during appending.
     */
    public void appendLine(String filePath, String line) {
        lock.writeLock().lock();
        log.info("WRITE LOCK acquired — appending to: {}", filePath);

        try {
            ensureFileExists(filePath);

            // Using BufferedWriter in APPEND mode
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(line);
                writer.newLine();
            }

            log.info("APPEND SUCCESS — line appended to: {}", filePath);

        } catch (IOException e) {
            log.error("APPEND FAILED — Error appending to file: {} | Cause: {}", filePath, e.getMessage());
            throw new DataAccessException("Failed to append to file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
            log.debug("WRITE LOCK released — file: {}", filePath);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPER — Encapsulated File Initialization
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Ensures the file and its parent directories exist before any I/O operation.
     * This is ENCAPSULATED — callers never need to worry about file creation.
     *
     * @param filePath Path to the file to check/create.
     */
    private void ensureFileExists(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                boolean dirsCreated = file.getParentFile().mkdirs();
                if (dirsCreated) {
                    log.info("INIT — Created parent directories for: {}", filePath);
                }
            }
            if (!file.exists()) {
                boolean fileCreated = file.createNewFile();
                if (fileCreated) {
                    log.info("INIT — Created new data file: {}", filePath);
                }
            }
        } catch (IOException e) {
            log.error("INIT FAILED — Cannot create file: {} | Cause: {}", filePath, e.getMessage());
            throw new DataAccessException("Failed to initialize file: " + filePath, e);
        }
    }
}
