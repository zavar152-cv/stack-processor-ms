package ru.itmo.zavar.highloadprojectauthservice.dto.inner.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ValidateTokenResponse(String username, List<String> authorities) {
}
