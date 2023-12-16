package ru.itmo.zavar.highload.authservice.dto.outer.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SignInResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxNzAxMTIxOTI0LCJleHAiOjE3MDExMjMzNjR9.TEA-S9YEk53iFg1GQL1_pimcoXciroMdLEZJBu7GSAo") String token) {
}
