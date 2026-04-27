package id.ac.ui.cs.advprog.yomu.clan.internal.service;

import id.ac.ui.cs.advprog.yomu.auth.AuthFacade;
import id.ac.ui.cs.advprog.yomu.clan.internal.repository.InMemoryClanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClanServiceImplTest {

    private ClanService clanService;
    private AuthFacade authFacade;

    @BeforeEach
    void setUp() {
        authFacade = mock(AuthFacade.class);
        clanService = new ClanServiceImpl(new InMemoryClanRepository(), authFacade);
    }

    private void mockUserName(UUID userId) {
        id.ac.ui.cs.advprog.yomu.auth.UserDto dto = id.ac.ui.cs.advprog.yomu.auth.UserDto.builder()
            .id(userId)
            .username("user-" + userId.toString().substring(0, 8))
            .displayName("Member " + userId.toString().substring(0, 6))
            .role("PELAJAR")
            .build();
        when(authFacade.getUserById(userId)).thenReturn(java.util.Optional.of(dto));
    }

    @Test
    void createClanSetsCreatorAsOwnerAndFirstMember() {
        UUID creatorUserId = UUID.randomUUID();
        mockUserName(creatorUserId);

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
    void createClanRejectsWhenCreatorAlreadyInAnotherClan() {
        UUID userId = UUID.randomUUID();
        mockUserName(userId);
        clanService.createClan(userId, "Clan Pertama");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.createClan(userId, "Clan Kedua")
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void joinClanAddsMemberToClan() {
        UUID ownerUserId = UUID.randomUUID();
        mockUserName(ownerUserId);
        ClanResponse createdClan = clanService.createClan(ownerUserId, "Diskusi Hebat");
        UUID newMember = UUID.randomUUID();
        mockUserName(newMember);

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
        mockUserName(ownerUserId);
        ClanResponse createdClan = clanService.createClan(ownerUserId, "Komunitas Cermat");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.joinClan(ownerUserId, createdClan.clanId())
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void joinClanRejectsWhenUserAlreadyInAnotherClan() {
        UUID userId = UUID.randomUUID();
        mockUserName(userId);
        clanService.createClan(userId, "Clan A");
        UUID ownerB = UUID.randomUUID();
        mockUserName(ownerB);
        ClanResponse clanB = clanService.createClan(ownerB, "Clan B");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.joinClan(userId, clanB.clanId())
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void joinClanRejectsWhenClanIsFull() {
        UUID owner = UUID.randomUUID();
        mockUserName(owner);
        ClanResponse createdClan = clanService.createClan(owner, "Kapasitas Penuh");
        UUID m1 = UUID.randomUUID(); mockUserName(m1); clanService.joinClan(m1, createdClan.clanId());
        UUID m2 = UUID.randomUUID(); mockUserName(m2); clanService.joinClan(m2, createdClan.clanId());
        UUID m3 = UUID.randomUUID(); mockUserName(m3); clanService.joinClan(m3, createdClan.clanId());
        UUID m4 = UUID.randomUUID(); mockUserName(m4); clanService.joinClan(m4, createdClan.clanId());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> {
                UUID extra = UUID.randomUUID();
                mockUserName(extra);
                clanService.joinClan(extra, createdClan.clanId());
            }
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void leaveClanAllowsNonOwnerMemberToLeave() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        mockUserName(ownerId);
        mockUserName(memberId);
        ClanResponse createdClan = clanService.createClan(ownerId, "Leave Test Clan");
        clanService.joinClan(memberId, createdClan.clanId());

        ClanResponse response = clanService.leaveClan(memberId, createdClan.clanId());

        assertThat(response.memberUserIds()).containsExactly(ownerId);
    }

    @Test
    void leaveClanRejectsOwnerUntilOwnershipTransferred() {
        UUID ownerId = UUID.randomUUID();
        mockUserName(ownerId);
        ClanResponse createdClan = clanService.createClan(ownerId, "Owner Leave Clan");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.leaveClan(ownerId, createdClan.clanId())
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void transferOwnershipAllowsOwnerToLeaveAfterTransfer() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        mockUserName(ownerId);
        mockUserName(memberId);
        ClanResponse createdClan = clanService.createClan(ownerId, "Transfer Clan");
        clanService.joinClan(memberId, createdClan.clanId());

        ClanResponse transferred = clanService.transferOwnership(ownerId, createdClan.clanId(), memberId);
        ClanResponse afterLeave = clanService.leaveClan(ownerId, createdClan.clanId());

        assertThat(transferred.ownerUserId()).isEqualTo(memberId);
        assertThat(afterLeave.memberUserIds()).containsExactly(memberId);
    }

    @Test
    void transferOwnershipRejectsWhenCallerIsNotOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        mockUserName(ownerId);
        mockUserName(memberId);
        ClanResponse createdClan = clanService.createClan(ownerId, "Transfer Rule Clan");
        clanService.joinClan(memberId, createdClan.clanId());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> clanService.transferOwnership(memberId, createdClan.clanId(), ownerId)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
