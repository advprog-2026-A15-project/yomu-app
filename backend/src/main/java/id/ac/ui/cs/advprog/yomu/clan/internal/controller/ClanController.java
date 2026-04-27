package id.ac.ui.cs.advprog.yomu.clan.internal.controller;

import id.ac.ui.cs.advprog.yomu.auth.AuthFacade;
import id.ac.ui.cs.advprog.yomu.clan.internal.service.ClanResponse;
import id.ac.ui.cs.advprog.yomu.clan.internal.service.ClanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/clans")
public class ClanController {

    private final ClanService clanService;
    private final AuthFacade authFacade;

    public ClanController(ClanService clanService, AuthFacade authFacade) {
        this.clanService = clanService;
        this.authFacade = authFacade;
    }

    @GetMapping
    public List<ClanResponse> listClans() {
        return clanService.listClans();
    }

    @PostMapping
    public ResponseEntity<ClanResponse> createClan(
        @Valid @RequestBody CreateClanRequest request
    ) {
        ClanResponse response = clanService.createClan(requireAuthenticatedUserId(), request.clanName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{clanId}/members")
    public ClanResponse joinClan(@PathVariable UUID clanId) {
        return clanService.joinClan(requireAuthenticatedUserId(), clanId);
    }

    @DeleteMapping("/{clanId}/members/me")
    public ClanResponse leaveClan(@PathVariable UUID clanId) {
        return clanService.leaveClan(requireAuthenticatedUserId(), clanId);
    }

    @PutMapping("/{clanId}/owner")
    public ClanResponse transferOwnership(
        @PathVariable UUID clanId,
        @Valid @RequestBody TransferOwnershipRequest request
    ) {
        return clanService.transferOwnership(requireAuthenticatedUserId(), clanId, request.newOwnerUserId());
    }

    private UUID requireAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<UUID> authenticatedUserId = Optional.ofNullable(authFacade.getAuthenticatedUserId(authentication))
            .orElse(Optional.empty());
        return authenticatedUserId
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }
}
