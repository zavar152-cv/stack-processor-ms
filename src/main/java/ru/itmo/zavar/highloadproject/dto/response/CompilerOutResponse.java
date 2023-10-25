package ru.itmo.zavar.highloadproject.dto.response;

import lombok.Builder;

import java.util.ArrayList;

@Builder
public record CompilerOutResponse(Long id, ArrayList<Long> program, ArrayList<Long> data) {
}
