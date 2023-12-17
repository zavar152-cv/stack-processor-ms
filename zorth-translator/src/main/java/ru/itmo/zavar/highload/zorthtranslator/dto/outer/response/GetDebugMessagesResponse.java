package ru.itmo.zavar.highload.zorthtranslator.dto.outer.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record GetDebugMessagesResponse(@Schema(example = "1") Long id, @Schema(example = "1") Long requestId,
                                       @Schema(example = "[\"\",\"Tick: 1, TC: 1, Stage: FETCH, CR: 872415232 {NOPE}, IP: 0, AR: 0, TOS: 0, DS: null, RS: null, OUT: null, IN: k\"]") String[] text) {
}
