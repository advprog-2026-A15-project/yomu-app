package id.ac.ui.cs.advprog.yomu.clan.internal.controller;

import id.ac.ui.cs.advprog.yomu.clan.internal.service.ClanResponse;
import id.ac.ui.cs.advprog.yomu.clan.internal.service.ClanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clans")
public class ClanController {

    private final ClanService clanService;

    public ClanController(ClanService clanService) {
        this.clanService = clanService;
    }

    @GetMapping
    public List<ClanResponse> listClans() {
        return clanService.listClans();
    }

    @PostMapping
    public ResponseEntity<ClanResponse> createClan(@Valid @RequestBody CreateClanRequest request) {
        ClanResponse response = clanService.createClan(request.creatorUserId(), request.clanName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{clanId}/members")
    public ClanResponse joinClan(@PathVariable UUID clanId, @Valid @RequestBody JoinClanRequest request) {
        return clanService.joinClan(request.userId(), clanId);
    }
}
