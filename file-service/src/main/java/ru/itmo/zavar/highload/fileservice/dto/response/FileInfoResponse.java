package ru.itmo.zavar.highload.fileservice.dto.response;

import lombok.Builder;

@Builder
public record FileInfoResponse(Long id, String name, String ownerName) {

}
