package ru.itmo.zavar.highload.zorthprocessor.dto.outer.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CompileResponse(@Schema(example = "1") Long id, @Schema(example = "variable a\n3 a !\na @") String text,
                              @Schema(example = "true") Boolean debug) {
}