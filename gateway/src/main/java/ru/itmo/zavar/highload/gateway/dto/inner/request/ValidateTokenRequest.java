package ru.itmo.zavar.highload.gateway.dto.inner.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ValidateTokenRequest(@NotBlank String jwtToken) {
}
