package id.ac.ui.cs.advprog.yomu.clan.internal.service;

import id.ac.ui.cs.advprog.yomu.clan.internal.model.Clan;
import id.ac.ui.cs.advprog.yomu.clan.internal.repository.ClanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ClanServiceImpl implements ClanService {

    private final ClanRepository clanRepository;

    public ClanServiceImpl(ClanRepository clanRepository) {
        this.clanRepository = clanRepository;
    }

    @Override
    public ClanResponse createClan(UUID creatorUserId, String clanName) {
        if (creatorUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "creatorUserId is required");
        }
        if (clanName == null || clanName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clanName is required");
        }
        if (clanRepository.existsByNameIgnoreCase(clanName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Clan name already exists");
        }

        Clan createdClan = clanRepository.save(Clan.create(creatorUserId, clanName.trim()));
        return toResponse(createdClan);
    }

    @Override
    public ClanResponse joinClan(UUID userId, UUID clanId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (clanId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clanId is required");
        }

        Clan clan = clanRepository.findById(clanId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clan not found"));

        if (clan.isMember(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a clan member");
        }
        if (clan.isFull()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Clan is full");
        }

        clan.addMember(userId);
        clanRepository.save(clan);
        return toResponse(clan);
    }

    @Override
    public List<ClanResponse> listClans() {
        return clanRepository.findAll().stream()
            .map(this::toResponse)
            .sorted(Comparator.comparing(ClanResponse::clanName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private ClanResponse toResponse(Clan clan) {
        List<UUID> members = clan.getMemberUserIds().stream().sorted(Comparator.comparing(UUID::toString)).toList();
        return new ClanResponse(
            clan.getId(),
            clan.getName(),
            clan.getOwnerUserId(),
            members,
            members.size(),
            Clan.MAX_MEMBERS,
            !clan.isFull(),
            clan.getTier(),
            clan.getScore()
        );
    }
}
