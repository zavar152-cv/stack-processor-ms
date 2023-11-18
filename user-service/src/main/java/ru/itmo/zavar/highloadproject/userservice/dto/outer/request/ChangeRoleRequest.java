package ru.itmo.zavar.highloadproject.userservice.dto.outer.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ChangeRoleRequest(@NotBlank String role) {
}
