package id.ac.ui.cs.advprog.yomu.clan.internal.service;

import java.util.List;
import java.util.UUID;

public record ClanResponse(
    UUID clanId,
    String clanName,
    UUID ownerUserId,
    List<UUID> memberUserIds,
    int memberCount,
    int maxMembers,
    boolean joinable,
    String tier,
    double score
) {
}
