package com.sliit.registration.exception;

/**
 * Custom unchecked exception for file-based data access failures.
 *
 * OOP Concepts Demonstrated:
 * - INHERITANCE: Extends RuntimeException (unchecked exception hierarchy).
 * - ENCAPSULATION: Wraps low-level IOException details into a domain-specific exception,
 *   preventing upper layers from depending on java.io internals.
 * - INFORMATION HIDING: Controllers and Services never see raw IOException —
 *   they only catch this clean, application-level exception.
 *
 * Design Decision: Unchecked (RuntimeException) so that every @Service and @Controller
 * method does NOT need a throws clause — keeps the Spring MVC contract clean.
 */
public class DataAccessException extends RuntimeException {

    /**
     * Constructs a DataAccessException with a descriptive message.
     * @param message Human-readable description of what went wrong.
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a DataAccessException with a message and the root cause.
     * @param message Human-readable description.
     * @param cause   The original IOException that triggered the failure.
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
