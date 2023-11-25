package ru.itmo.zavar.highload.userservice.dto.outer.response;

import lombok.Builder;

@Builder
public record GetRequestsResponse(Long id, String text, Boolean debug) {
}
