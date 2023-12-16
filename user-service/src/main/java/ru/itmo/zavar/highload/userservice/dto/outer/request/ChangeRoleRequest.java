package ru.itmo.zavar.highload.userservice.dto.outer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ChangeRoleRequest(@Schema(example = "ROLE_VIP") @NotBlank String role) {
}
