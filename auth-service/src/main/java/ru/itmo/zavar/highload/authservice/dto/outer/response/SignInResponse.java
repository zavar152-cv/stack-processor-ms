package ru.itmo.zavar.highload.authservice.dto.outer.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SignInResponse(
        @Schema(example = "REPLACE_WITH_JWT_TOKEN") String token) {
}
