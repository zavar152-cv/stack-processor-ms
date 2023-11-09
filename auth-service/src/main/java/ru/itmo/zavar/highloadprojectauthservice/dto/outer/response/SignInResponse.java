package ru.itmo.zavar.highloadprojectauthservice.dto.outer.response;

import lombok.Builder;

@Builder
public record SignInResponse(String token) {
}
