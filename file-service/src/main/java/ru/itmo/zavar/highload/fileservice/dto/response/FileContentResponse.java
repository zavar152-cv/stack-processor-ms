package ru.itmo.zavar.highload.fileservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record FileContentResponse(@Schema(example = "1") Long id, @Schema(example = "input.zorth") String name,
                                  @Schema(example = "variable a\n3 a !\na @") String content) {
}
