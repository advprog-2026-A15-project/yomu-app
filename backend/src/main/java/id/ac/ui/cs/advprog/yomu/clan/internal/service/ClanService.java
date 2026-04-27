package id.ac.ui.cs.advprog.yomu.clan.internal.service;

import java.util.UUID;
import java.util.List;

public interface ClanService {

    ClanResponse createClan(UUID creatorUserId, String clanName);

    ClanResponse joinClan(UUID userId, UUID clanId);

    ClanResponse leaveClan(UUID userId, UUID clanId);

    ClanResponse transferOwnership(UUID currentOwnerUserId, UUID clanId, UUID newOwnerUserId);

    List<ClanResponse> listClans();
}
