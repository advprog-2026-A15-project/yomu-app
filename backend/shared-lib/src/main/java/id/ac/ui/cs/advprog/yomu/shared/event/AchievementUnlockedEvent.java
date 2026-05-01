package id.ac.ui.cs.advprog.yomu.achievements;

import java.time.Instant;
import java.util.UUID;

/**
 * Public contract emitted when a user unlocks an achievement.
 */
public record AchievementUnlockedEvent(
    UUID userId,
    String achievementCode,
    String achievementName,
    Instant occurredAt
) {
}
