package id.ac.ui.cs.advprog.yomu.achievements.internal.dto;

import id.ac.ui.cs.advprog.yomu.achievements.internal.model.AchievementMetric;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DailyMissionProgressResponse(
    UUID missionId,
    String code,
    String name,
    String description,
    AchievementMetric metric,
    int targetCount,
    int progress,
    int rewardPoints,
    LocalDate activeFrom,
    LocalDate activeUntil,
    boolean completed,
    boolean claimed,
    Instant claimedAt
) {
}
