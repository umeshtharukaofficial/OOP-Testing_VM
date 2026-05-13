package com.sliit.registration.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * FileHandler abstraction: Handles raw file IO operations.
 * Demonstrates Abstraction and Information Hiding.
 * Uses ReadWriteLocks to manage concurrency when accessing .txt files.
 */
@Component
public class FileHandler {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public List<String> readLines(String filePath) {
        lock.readLock().lock();
        List<String> lines = new ArrayList<>();
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                lines = Files.readAllLines(path);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
        return lines;
    }

    public void writeLines(String filePath, List<String> lines) {
        lock.writeLock().lock();
        try {
            Path path = Paths.get(filePath);
            Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void appendLine(String filePath, String line) {
        lock.writeLock().lock();
        try {
            Path path = Paths.get(filePath);
            Files.write(path, (line + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error appending to file: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}
