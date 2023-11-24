package ru.itmo.zavar.highload.zorthtranslator.dto.outer.response;

import lombok.Builder;

import java.util.ArrayList;

@Builder
public record GetCompilerOutResponse(Long id, Long requestId, ArrayList<Long> program, ArrayList<Long> data) {
}
