package ru.itmo.zavar.highloadprojectgateway.dto.inner.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ValidateTokenResponse(String username, List<String> authorities) {
}
