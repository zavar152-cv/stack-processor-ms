package ru.itmo.zavar.highloadproject.dto.request;

import lombok.Builder;

@Builder
public record ChangeRoleRequest(String username, String role) {
}
