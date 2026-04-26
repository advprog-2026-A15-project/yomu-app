package id.ac.ui.cs.advprog.yomu.clan;

import java.time.Instant;
import java.util.UUID;

/**
 * Public contract emitted when a learner performs an activity that affects league progress.
 */
public record LeagueActivityEvent(
    UUID userId,
    UUID leagueId,
    UUID activityId,
    String activityType,
    Instant occurredAt
) {
}
