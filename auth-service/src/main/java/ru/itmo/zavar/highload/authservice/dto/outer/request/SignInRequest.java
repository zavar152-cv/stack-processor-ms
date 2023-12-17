package ru.itmo.zavar.highload.authservice.dto.outer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SignInRequest(@Schema(example = "admin") @NotBlank @Size(min = 5, max = 25) String username,
                            @Schema(example = "admin") @NotBlank String password) {
}
