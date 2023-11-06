package ru.itmo.zavar.highloadprojectrequestservice.dto.inner.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RequestServiceRequest(@NotBlank String text, @NotNull Boolean debug) {
}