package ru.itmo.zavar.highloadproject.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GetProcessorOutRequest(@NotNull Long requestId) {
}
