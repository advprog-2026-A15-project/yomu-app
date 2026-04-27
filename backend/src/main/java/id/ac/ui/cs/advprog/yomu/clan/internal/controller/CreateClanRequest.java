package id.ac.ui.cs.advprog.yomu.clan.internal.controller;

import jakarta.validation.constraints.NotBlank;

public record CreateClanRequest(
    @NotBlank String clanName
) {
}
