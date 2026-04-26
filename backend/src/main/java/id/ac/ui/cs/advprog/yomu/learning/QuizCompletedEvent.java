package id.ac.ui.cs.advprog.yomu.learning;

import java.time.Instant;
import java.util.UUID;

/**
 * Public contract emitted when a learner completes a quiz.
 */
public record QuizCompletedEvent(
    UUID userId,
    UUID quizId,
    String quizSlug,
    int score,
    Instant occurredAt
) {
}
