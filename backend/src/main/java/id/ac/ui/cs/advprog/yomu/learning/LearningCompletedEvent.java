package id.ac.ui.cs.advprog.yomu.learning;

import java.time.Instant;
import java.util.UUID;

/**
 * Public contract emitted when a learner completes a reading item.
 */
public record LearningCompletedEvent(
    UUID userId,
    UUID bacaanId,
    String bacaanSlug,
    Instant occurredAt
) {
}
