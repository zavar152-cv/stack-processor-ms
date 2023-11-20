package ru.itmo.zavar.highload.zorthtranslator.dto.outer.response;

import lombok.Builder;

import java.util.ArrayList;

@Builder
public record GetCompilerOutResponse(Long id, ArrayList<Long> program, ArrayList<Long> data) {
}
