package id.ac.ui.cs.advprog.yomu.achievements.internal.dto;

import java.time.Instant;
import java.util.UUID;

public record ClaimRewardResponse(
    UUID missionId,
    UUID userId,
    int rewardPoints,
    boolean claimed,
    Instant claimedAt
) {
}
