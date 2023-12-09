package ru.itmo.zavar.highload.fileservice.dto.response;

import lombok.Builder;

@Builder
public record FileContentResponse(Long id, String name, String content) {

}
