package id.ac.ui.cs.advprog.yomu.clan.internal.repository;

import id.ac.ui.cs.advprog.yomu.clan.internal.model.Clan;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface ClanRepository {

    Clan save(Clan clan);

    Optional<Clan> findById(UUID clanId);

    List<Clan> findAll();

    Optional<Clan> findByMemberUserId(UUID userId);

    Optional<Clan> findByOwnerUserId(UUID ownerUserId);

    boolean existsByNameIgnoreCase(String clanName);
}
