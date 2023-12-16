package ru.itmo.zavar.highload.zorthtranslator.dto.outer.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.ArrayList;

@Builder
public record GetCompilerOutResponse(@Schema(example = "1") Long id, @Schema(example = "1") Long requestId,
                                     @Schema(example = "[721420291,989855748,637534208,989855748,671088640,0]") ArrayList<Long> program,
                                     @Schema(example = "[0,0,0,3,0]") ArrayList<Long> data) {
}
