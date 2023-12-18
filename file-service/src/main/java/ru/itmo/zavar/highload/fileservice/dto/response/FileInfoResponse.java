package ru.itmo.zavar.highload.fileservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record FileInfoResponse(@Schema(example = "1") Long id, @Schema(example = "input.zorth") String name,
                               @Schema(example = "admin") String ownerName) {
}
