package id.ac.ui.cs.advprog.yomu.clan.internal.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateClanRequest(
    @NotNull UUID creatorUserId,
    @NotBlank String clanName
) {
}
