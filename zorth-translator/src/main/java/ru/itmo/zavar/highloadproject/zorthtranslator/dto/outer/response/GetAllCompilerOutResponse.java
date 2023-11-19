package ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response;

import lombok.Builder;

import java.util.ArrayList;

@Builder
public record GetAllCompilerOutResponse(Long id, Long requestId, ArrayList<Long> program, ArrayList<Long> data) {
}
