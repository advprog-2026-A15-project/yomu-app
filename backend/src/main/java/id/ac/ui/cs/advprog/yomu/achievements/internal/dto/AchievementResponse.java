package id.ac.ui.cs.advprog.yomu.achievements.internal.dto;

import id.ac.ui.cs.advprog.yomu.achievements.internal.model.AchievementMetric;

import java.time.Instant;
import java.util.UUID;

public record AchievementResponse(
    UUID achievementId,
    String code,
    String name,
    String description,
    AchievementMetric metric,
    int milestone,
    boolean active,
    Instant createdAt
) {
}
