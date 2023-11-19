package ru.itmo.zavar.highloadproject.userservice.dto.inner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RoleDTO(@NotNull Long id, @NotBlank String name) {
}