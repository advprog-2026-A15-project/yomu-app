package id.ac.ui.cs.advprog.yomu.clan.internal.service;

import id.ac.ui.cs.advprog.yomu.clan.internal.repository.InMemoryClanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClanServiceImplTest {

    private ClanService clanService;

    @BeforeEach
    void setUp() {
        clanService = new ClanServiceImpl(new InMemoryClanRepository());
    }

    @Test
    void createClanSetsCreatorAsOwnerAndFirstMember() {
        UUID creatorUserId = UUID.randomUUID();

        ClanResponse response = clanService.createClan(creatorUserId, "Nusantara Readers");

        assertThat(response.clanName()).isEqualTo("Nusantara Readers");
        assertThat(response.ownerUserId()).isEqualTo(creatorUserId);
        assertThat(response.memberUserIds()).containsExactly(creatorUserId);
        assertThat(response.memberCount()).isEqualTo(1);
        assertThat(response.maxMembers()).isEqualTo(5);
        assertThat(response.joinable()).isTrue();
        assertThat(response.tier()).isEqualTo("BRONZE");
        assertThat(response.score()).isEqualTo(0.0d);
    }

    @Test
    void createClanRejectsDuplicateNameIgnoringCase() {
        clanService.createClan(UUID.randomUUID(), "Nusantara Readers");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.createClan(UUID.randomUUID(), "nusantara readers")
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void joinClanAddsMemberToClan() {
        UUID ownerUserId = UUID.randomUUID();
        ClanResponse createdClan = clanService.createClan(ownerUserId, "Diskusi Hebat");
        UUID newMember = UUID.randomUUID();

        ClanResponse joinedClan = clanService.joinClan(newMember, createdClan.clanId());

        assertThat(joinedClan.memberUserIds()).contains(ownerUserId, newMember);
        assertThat(joinedClan.memberCount()).isEqualTo(2);
    }

    @Test
    void joinClanRejectsMissingClan() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.joinClan(UUID.randomUUID(), UUID.randomUUID())
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void joinClanRejectsDuplicateMembership() {
        UUID ownerUserId = UUID.randomUUID();
        ClanResponse createdClan = clanService.createClan(ownerUserId, "Komunitas Cermat");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.joinClan(ownerUserId, createdClan.clanId())
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void joinClanRejectsWhenClanIsFull() {
        ClanResponse createdClan = clanService.createClan(UUID.randomUUID(), "Kapasitas Penuh");
        clanService.joinClan(UUID.randomUUID(), createdClan.clanId());
        clanService.joinClan(UUID.randomUUID(), createdClan.clanId());
        clanService.joinClan(UUID.randomUUID(), createdClan.clanId());
        clanService.joinClan(UUID.randomUUID(), createdClan.clanId());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.joinClan(UUID.randomUUID(), createdClan.clanId())
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
