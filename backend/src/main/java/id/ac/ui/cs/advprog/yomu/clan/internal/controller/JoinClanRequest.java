package id.ac.ui.cs.advprog.yomu.clan.internal.controller;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinClanRequest(
    @NotNull UUID userId
) {
}
