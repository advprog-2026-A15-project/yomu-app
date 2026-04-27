package id.ac.ui.cs.advprog.yomu.clan.internal.controller;

import id.ac.ui.cs.advprog.yomu.auth.AuthFacade;
import id.ac.ui.cs.advprog.yomu.clan.internal.service.ClanMemberSummary;
import id.ac.ui.cs.advprog.yomu.clan.internal.service.ClanResponse;
import id.ac.ui.cs.advprog.yomu.clan.internal.service.ClanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClanControllerTest {

    @Mock
    private ClanService clanService;
    @Mock
    private AuthFacade authFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ClanController(clanService, authFacade)).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createClanReturnsCreatedPayload() throws Exception {
        UUID creatorId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID clanId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        setAuthenticatedUser(creatorId);
        when(clanService.createClan(creatorId, "Nusantara Readers"))
            .thenReturn(new ClanResponse(
                clanId,
                "Nusantara Readers",
                creatorId,
                List.of(creatorId),
                List.of(new ClanMemberSummary(creatorId, "Creator", true)),
                1,
                5,
                true,
                "BRONZE",
                0.0d
            ));

        mockMvc.perform(post("/api/clans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clanName": "Nusantara Readers"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.clanId").value("22222222-2222-2222-2222-222222222222"))
            .andExpect(jsonPath("$.clanName").value("Nusantara Readers"))
            .andExpect(jsonPath("$.ownerUserId").value("11111111-1111-1111-1111-111111111111"))
            .andExpect(jsonPath("$.memberUserIds[0]").value("11111111-1111-1111-1111-111111111111"))
            .andExpect(jsonPath("$.memberCount").value(1))
            .andExpect(jsonPath("$.maxMembers").value(5))
            .andExpect(jsonPath("$.joinable").value(true))
            .andExpect(jsonPath("$.tier").value("BRONZE"))
            .andExpect(jsonPath("$.score").value(0.0d));

        verify(clanService).createClan(creatorId, "Nusantara Readers");
    }

    @Test
    void listClansReturnsArrayPayload() throws Exception {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID clanId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(clanService.listClans()).thenReturn(List.of(
            new ClanResponse(clanId, "Nusantara Readers", ownerId, List.of(ownerId), List.of(new ClanMemberSummary(ownerId, "Owner", true)), 1, 5, true, "BRONZE", 0.0d)
        ));

        mockMvc.perform(get("/api/clans"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].clanId").value("22222222-2222-2222-2222-222222222222"))
            .andExpect(jsonPath("$[0].memberCount").value(1));

        verify(clanService).listClans();
    }

    @Test
    void joinClanReturnsUpdatedMembers() throws Exception {
        UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID clanId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        setAuthenticatedUser(userId);
        when(clanService.joinClan(userId, clanId))
            .thenReturn(new ClanResponse(
                clanId,
                "Nusantara Readers",
                ownerId,
                List.of(ownerId, userId),
                List.of(
                    new ClanMemberSummary(ownerId, "Owner", true),
                    new ClanMemberSummary(userId, "Member", false)
                ),
                2,
                5,
                true,
                "BRONZE",
                0.0d
            ));

        mockMvc.perform(post("/api/clans/22222222-2222-2222-2222-222222222222/members")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memberUserIds[1]").value("33333333-3333-3333-3333-333333333333"))
            .andExpect(jsonPath("$.memberCount").value(2));

        verify(clanService).joinClan(userId, clanId);
    }

    @Test
    void joinClanReturnsNotFoundWhenClanMissing() throws Exception {
        UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        setAuthenticatedUser(userId);
        when(clanService.joinClan(
            userId,
            UUID.fromString("22222222-2222-2222-2222-222222222222")
        )).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Clan not found"));

        mockMvc.perform(post("/api/clans/22222222-2222-2222-2222-222222222222/members")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void leaveClanUsesAuthenticatedUser() throws Exception {
        UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID clanId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        setAuthenticatedUser(userId);
        when(clanService.leaveClan(userId, clanId))
            .thenReturn(new ClanResponse(clanId, "Nusantara Readers", ownerId, List.of(ownerId), List.of(new ClanMemberSummary(ownerId, "Owner", true)), 1, 5, true, "BRONZE", 0.0d));

        mockMvc.perform(delete("/api/clans/22222222-2222-2222-2222-222222222222/members/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memberCount").value(1));

        verify(clanService).leaveClan(userId, clanId);
    }

    @Test
    void transferOwnershipUsesAuthenticatedUser() throws Exception {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID newOwnerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID clanId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        setAuthenticatedUser(ownerId);
        when(clanService.transferOwnership(ownerId, clanId, newOwnerId))
            .thenReturn(new ClanResponse(
                clanId,
                "Nusantara Readers",
                newOwnerId,
                List.of(ownerId, newOwnerId),
                List.of(
                    new ClanMemberSummary(ownerId, "Owner", false),
                    new ClanMemberSummary(newOwnerId, "New Owner", true)
                ),
                2,
                5,
                true,
                "BRONZE",
                0.0d
            ));

        mockMvc.perform(put("/api/clans/22222222-2222-2222-2222-222222222222/owner")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "newOwnerUserId": "33333333-3333-3333-3333-333333333333"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ownerUserId").value("33333333-3333-3333-3333-333333333333"));

        verify(clanService).transferOwnership(ownerId, clanId, newOwnerId);
    }

    @Test
    void createClanRejectsWhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/clans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "clanName": "Nusantara Readers"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    private void setAuthenticatedUser(UUID userId) {
        when(authFacade.getAuthenticatedUserId(any(Authentication.class))).thenReturn(Optional.of(userId));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("principal", null, List.of())
        );
    }
}
