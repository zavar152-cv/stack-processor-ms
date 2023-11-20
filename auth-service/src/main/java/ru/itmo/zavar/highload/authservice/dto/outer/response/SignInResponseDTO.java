package ru.itmo.zavar.highload.authservice.dto.outer.response;

import lombok.Builder;

@Builder
public record SignInResponseDTO(String token) {
}
