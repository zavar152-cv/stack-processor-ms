package ru.itmo.zavar.highloadproject.dto.outer.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GetDebugMessagesRequest(@NotNull Long id) {
}
