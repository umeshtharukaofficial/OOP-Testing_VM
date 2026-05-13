package com.sliit.registration.service;

import com.sliit.registration.dto.LoginDto;
import com.sliit.registration.model.User;
import com.sliit.registration.repository.UserRepository;
import com.sliit.registration.service.interfaces.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║             AuthServiceImpl — implements IAuthService                    ║
 * ║  Concrete implementation of the authentication contract.                 ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. ABSTRACTION: Implements the IAuthService interface — the Controller
 *    only knows the interface contract, never this concrete class.
 *
 * 2. POLYMORPHISM: Spring resolves IAuthService → AuthServiceImpl at runtime
 *    via component scanning. If a second implementation were registered,
 *    @Primary or @Qualifier would disambiguate.
 *
 * 3. ENCAPSULATION: The UserRepository dependency is private and final.
 *    Constructor injection ensures immutability (no setter mutation).
 *
 * 4. INFORMATION HIDING: The authentication strategy (file-based scan with
 *    plain-text comparison) is invisible to any caller of IAuthService.
 *
 * CONSTRUCTOR INJECTION (Industry Standard):
 * ─────────────────────────────────────────────────────────────────────────────
 * - NO @Autowired on fields — all dependencies are final + constructor-injected.
 * - Spring automatically detects the single constructor and injects beans.
 * - Benefits: immutability, explicit dependencies, easier testing.
 */
@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {

    // ── Private, final — Encapsulation + Immutability ──
    private final UserRepository userRepository;

    // ── Constructor Injection (no @Autowired needed for single constructor) ──
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("AuthServiceImpl initialized via constructor injection");
    }

    /**
     * {@inheritDoc}
     *
     * Implementation Details:
     * - Extracts username/password from the validated LoginDto.
     * - Scans the file-backed UserRepository for a matching record.
     * - Returns the polymorphic User reference (Student/Moderator/Admin).
     */
    @Override
    public Optional<User> authenticate(LoginDto loginDto) {
        log.info("authenticate() — DTO received for username: '{}'", loginDto.getUsername());

        String username = loginDto.getUsername().trim();
        String password = loginDto.getPassword();

        log.debug("authenticate() — Querying UserRepository for username: '{}'", username);

        Optional<User> result = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username) && u.getPasswordHash().equals(password))
                .findFirst();

        if (result.isPresent()) {
            log.info("authenticate() — SUCCESS — User '{}' authenticated (role: {})",
                    username, result.get().getRole());
        } else {
            log.warn("authenticate() — FAILED — Invalid credentials for username: '{}'", username);
        }

        return result;
    }
}
