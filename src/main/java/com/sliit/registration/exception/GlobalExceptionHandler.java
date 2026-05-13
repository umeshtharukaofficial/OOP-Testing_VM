package com.sliit.registration.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║             GLOBAL EXCEPTION HANDLER (@ControllerAdvice)                 ║
 * ║  Centralised error handling — users NEVER see raw stack traces.          ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. ABSTRACTION: A single class intercepts ALL controller exceptions across
 *    the entire application. Individual controllers don't need try-catch blocks.
 *
 * 2. POLYMORPHISM: The @ExceptionHandler methods use method overloading —
 *    Spring dispatches to the most specific handler based on the exception type.
 *
 * 3. INFORMATION HIDING: Raw IOException details, stack traces, and internal
 *    file paths are logged server-side but NEVER shown to the end user.
 *    The user sees only a clean, human-readable flash message.
 *
 * SPRING MVC PATTERN:
 * ─────────────────────────────────────────────────────────────────────────────
 * @ControllerAdvice makes this a cross-cutting concern that applies to every
 * @Controller in the application. It acts as the global error safety net.
 *
 * REDIRECT STRATEGY:
 * ─────────────────────────────────────────────────────────────────────────────
 * Instead of rendering a static error page, each handler:
 *   1. Logs the full exception for debugging (SLF4J).
 *   2. Adds a user-friendly error message as a flash attribute.
 *   3. Redirects the user back to the page they came from (via Referer header).
 *   4. Falls back to the login page if no Referer is available.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════════════════════════════
    //  HANDLER 1: DTO Validation Failures
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Catches validation errors from @Valid on DTOs (LoginDto, UserRegistrationDto,
     * CourseModuleDto). Triggered when Jakarta Bean Validation constraints
     * (@NotBlank, @Size, @Min, @Pattern) fail.
     *
     * @param ex      The validation exception containing all field errors.
     * @param request The HTTP request (used to extract the Referer header).
     * @param ra      Flash attributes for the redirect.
     * @return Redirect back to the originating page.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationException(MethodArgumentNotValidException ex,
                                             HttpServletRequest request,
                                             RedirectAttributes ra) {

        // Extract the first field error message for the user
        String fieldError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid input provided.");

        log.warn("VALIDATION ERROR — {} | URI: {} | Details: {}",
                fieldError, request.getRequestURI(), ex.getMessage());

        ra.addFlashAttribute("error", "Validation Error: " + fieldError);
        return "redirect:" + resolveRedirectUrl(request);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HANDLER 2: File I/O Failures (Custom DataAccessException)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Catches our custom DataAccessException thrown by FileStorageService
     * and all Repository classes when file read/write operations fail.
     *
     * INFORMATION HIDING: The raw file path and IOException details are logged
     * server-side but the user only sees "A data storage error occurred."
     *
     * @param ex      The custom DataAccessException.
     * @param request The HTTP request.
     * @param ra      Flash attributes for the redirect.
     * @return Redirect back to the originating page.
     */
    @ExceptionHandler(DataAccessException.class)
    public String handleDataAccessException(DataAccessException ex,
                                             HttpServletRequest request,
                                             RedirectAttributes ra) {

        log.error("DATA ACCESS ERROR — {} | URI: {} | Root Cause: {}",
                ex.getMessage(), request.getRequestURI(),
                ex.getCause() != null ? ex.getCause().getMessage() : "N/A", ex);

        ra.addFlashAttribute("error",
                "A data storage error occurred. Please try again or contact the administrator.");
        return "redirect:" + resolveRedirectUrl(request);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HANDLER 3: IllegalArgumentException (Bad Role, Invalid Input)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Catches IllegalArgumentException thrown by services when input
     * parameters are logically invalid (e.g., unknown role in registration).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                         HttpServletRequest request,
                                         RedirectAttributes ra) {

        log.warn("ILLEGAL ARGUMENT — {} | URI: {}", ex.getMessage(), request.getRequestURI());

        ra.addFlashAttribute("error", "Invalid request: " + ex.getMessage());
        return "redirect:" + resolveRedirectUrl(request);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HANDLER 4: Catch-All Fallback (Unexpected Bugs)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * The ultimate safety net. Catches ANY unhandled exception that slips
     * through the specific handlers above. This ensures the user NEVER
     * sees the Spring Whitelabel Error Page or a raw Java stack trace.
     *
     * The full stack trace is logged at ERROR level for debugging.
     *
     * @param ex      Any unhandled exception.
     * @param request The HTTP request.
     * @param ra      Flash attributes for the redirect.
     * @return Redirect to login page (safest fallback).
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex,
                                          HttpServletRequest request,
                                          RedirectAttributes ra) {

        log.error("UNEXPECTED ERROR — {} | URI: {} | Exception Type: {}",
                ex.getMessage(), request.getRequestURI(), ex.getClass().getSimpleName(), ex);

        ra.addFlashAttribute("error",
                "An unexpected error occurred. Our team has been notified.");
        return "redirect:" + resolveRedirectUrl(request);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPER — Intelligent Redirect Resolution
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Determines the best page to redirect the user back to after an error.
     *
     * Strategy:
     * 1. Check the HTTP Referer header — if present, redirect back to it.
     * 2. If no Referer, fall back to the login page (safe default).
     *
     * ENCAPSULATION: This resolution logic is private — no external class
     * can access or override it.
     *
     * @param request The current HTTP request.
     * @return The redirect URL path.
     */
    private String resolveRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");

        if (referer != null && !referer.isBlank()) {
            // Extract just the path from the full URL (remove protocol + host)
            try {
                java.net.URI uri = java.net.URI.create(referer);
                String path = uri.getPath();
                log.debug("resolveRedirectUrl() — Redirecting back to: {}", path);
                return path;
            } catch (Exception e) {
                log.debug("resolveRedirectUrl() — Failed to parse Referer, falling back to /login");
            }
        }

        log.debug("resolveRedirectUrl() — No Referer header, redirecting to /login");
        return "/login";
    }
}
