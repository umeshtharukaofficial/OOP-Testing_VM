package com.sliit.registration.model;

/**
 * Core interface enforcing File persistability across the system.
 */
public interface FilePersistable {
    String toFileString();
    void fromFileString(String line);
}
