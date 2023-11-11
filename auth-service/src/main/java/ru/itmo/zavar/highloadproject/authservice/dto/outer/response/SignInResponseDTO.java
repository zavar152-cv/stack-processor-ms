package ru.itmo.zavar.highloadproject.authservice.dto.outer.response;

import lombok.Builder;

@Builder
public record SignInResponseDTO(String token) {
}
