package ru.itmo.zavar.highloadprojectauthservice.dto.inner.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ValidateTokenRequest(@NotBlank String jwtToken) {
}
