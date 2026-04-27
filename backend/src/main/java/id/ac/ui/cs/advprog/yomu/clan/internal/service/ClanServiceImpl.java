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
        if (clanRepository.findByMemberUserId(creatorUserId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already in a clan");
        }
        if (clanRepository.findByOwnerUserId(creatorUserId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already owns a clan");
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

        Clan currentClan = clanRepository.findByMemberUserId(userId).orElse(null);
        if (currentClan != null) {
            if (currentClan.getId().equals(clanId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a clan member");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already in another clan");
        }
        if (clan.isFull()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Clan is full");
        }

        clan.addMember(userId);
        clanRepository.save(clan);
        return toResponse(clan);
    }

    @Override
    public ClanResponse leaveClan(UUID userId, UUID clanId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (clanId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clanId is required");
        }

        Clan clan = clanRepository.findById(clanId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clan not found"));

        if (!clan.isMember(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is not a member of this clan");
        }
        if (clan.getOwnerUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner must transfer ownership before leaving clan");
        }

        clan.removeMember(userId);
        clanRepository.save(clan);
        return toResponse(clan);
    }

    @Override
    public ClanResponse transferOwnership(UUID currentOwnerUserId, UUID clanId, UUID newOwnerUserId) {
        if (currentOwnerUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentOwnerUserId is required");
        }
        if (clanId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clanId is required");
        }
        if (newOwnerUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newOwnerUserId is required");
        }

        Clan clan = clanRepository.findById(clanId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clan not found"));

        if (!clan.getOwnerUserId().equals(currentOwnerUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can transfer ownership");
        }
        if (!clan.isMember(newOwnerUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "New owner must be an existing member");
        }

        clan.transferOwnership(newOwnerUserId);
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
