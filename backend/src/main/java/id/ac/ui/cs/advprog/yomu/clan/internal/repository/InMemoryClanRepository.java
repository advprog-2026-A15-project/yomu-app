package id.ac.ui.cs.advprog.yomu.clan.internal.repository;

import id.ac.ui.cs.advprog.yomu.clan.internal.model.Clan;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryClanRepository implements ClanRepository {

    private final Map<UUID, Clan> clansById = new ConcurrentHashMap<>();
    private final Map<String, UUID> clanIdsByName = new ConcurrentHashMap<>();

    @Override
    public Clan save(Clan clan) {
        clansById.put(clan.getId(), clan);
        clanIdsByName.put(normalizeName(clan.getName()), clan.getId());
        return clan;
    }

    @Override
    public Optional<Clan> findById(UUID clanId) {
        return Optional.ofNullable(clansById.get(clanId));
    }

    @Override
    public List<Clan> findAll() {
        return clansById.values().stream().toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String clanName) {
        return clanIdsByName.containsKey(normalizeName(clanName));
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
