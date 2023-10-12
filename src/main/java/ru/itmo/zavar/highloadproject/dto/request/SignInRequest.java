package ru.itmo.zavar.highloadproject.dto.request;

import lombok.Builder;

@Builder
public record SignInRequest(String username, String password) {
}
