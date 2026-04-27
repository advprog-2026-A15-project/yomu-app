package id.ac.ui.cs.advprog.yomu.clan.internal.service;

import java.util.UUID;

public record ClanMemberSummary(
    UUID userId,
    String displayName,
    boolean owner
) {
}
