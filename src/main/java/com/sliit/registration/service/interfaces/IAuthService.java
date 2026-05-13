package com.sliit.registration.service.interfaces;

import com.sliit.registration.dto.LoginDto;
import com.sliit.registration.model.User;

import java.util.Optional;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║                   IAuthService — Authentication Contract                 ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * OOP CONCEPTS DEMONSTRATED:
 * ─────────────────────────────────────────────────────────────────────────────
 * ABSTRACTION (Interface):
 *   This interface defines WHAT the authentication system can do, without
 *   specifying HOW. The implementation (AuthServiceImpl) provides the "how".
 *
 *   Controllers depend on this interface, NOT on the concrete class.
 *   This enables:
 *   - Loose coupling between layers.
 *   - Easy swapping of implementations (e.g., file-based → database-based).
 *   - Testability via mock implementations in unit tests.
 *
 * POLYMORPHISM:
 *   Spring injects the concrete AuthServiceImpl at runtime wherever
 *   IAuthService is declared. The caller doesn't know or care which
 *   implementation is running.
 */
public interface IAuthService {

    /**
     * Authenticates a user based on validated login credentials.
     *
     * @param loginDto Pre-validated login data transfer object.
     * @return Optional containing the authenticated User entity, or empty if invalid.
     */
    Optional<User> authenticate(LoginDto loginDto);
}
