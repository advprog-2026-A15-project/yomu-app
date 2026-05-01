package id.ac.ui.cs.advprog.yomu.achievements.internal.dto;

import id.ac.ui.cs.advprog.yomu.achievements.internal.model.AchievementMetric;

import java.time.Instant;
import java.util.UUID;

public record AchievementProgressResponse(
    UUID achievementId,
    String code,
    String name,
    String description,
    AchievementMetric metric,
    int milestone,
    int progress,
    boolean unlocked,
    Instant unlockedAt
) {
}
