package ru.itmo.zavar.highloadproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ExecuteRequest(@NotNull String[] input, @NotNull Long requestId) {
}
