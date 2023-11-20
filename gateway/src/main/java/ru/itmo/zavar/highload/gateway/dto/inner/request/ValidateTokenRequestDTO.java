package ru.itmo.zavar.highload.gateway.dto.inner.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ValidateTokenRequestDTO(@NotBlank String jwtToken) {
}
