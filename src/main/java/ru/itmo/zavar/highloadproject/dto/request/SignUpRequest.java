package ru.itmo.zavar.highloadproject.dto.request;

import lombok.Builder;

@Builder
public record SignUpRequest(String username, String password) {
}
