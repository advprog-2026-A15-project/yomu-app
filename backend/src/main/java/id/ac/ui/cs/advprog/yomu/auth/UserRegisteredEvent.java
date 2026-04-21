package id.ac.ui.cs.advprog.yomu.auth;

import java.time.Instant;
import java.util.UUID;

/**
 * Public contract emitted when a user account is created successfully.
 */
public record UserRegisteredEvent(
    UUID userId,
    String username,
    String email,
    Instant occurredAt
) {
}
