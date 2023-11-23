package ru.itmo.zavar.highload.zorthprocessor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.ExecuteRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.PipelineRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.response.ProcessorOutDTO;
import ru.itmo.zavar.highload.zorthprocessor.mapper.ProcessorOutEntityMapper;
import ru.itmo.zavar.highload.zorthprocessor.service.ZorthProcessorService;

@RestController
@RequiredArgsConstructor
public class ZorthProcessorController {
    private final ZorthProcessorService zorthProcessorService;
    private final ProcessorOutEntityMapper processorOutEntityMapper;

    @PostMapping("/pipeline")
    public Mono<ProcessorOutDTO> pipeline(@Valid @RequestBody PipelineRequest request, Authentication authentication) {
        return zorthProcessorService.pipeline(request.debug(), request.text(), request.input(), authentication)
                .map(processorOutEntityMapper::toDTO);
    }

    @PostMapping("/execute")
    public Mono<ProcessorOutDTO> execute(@Valid @RequestBody ExecuteRequest request, Authentication authentication) {
        return zorthProcessorService.startProcessorAndGetLogs(request.input(), request.requestId(), authentication)
                .map(processorOutEntityMapper::toDTO);
    }

    @GetMapping(value = "/processor-outs", params = "request-id")
    public Flux<ProcessorOutDTO> getAllProcessorOut(@RequestParam("request-id") Long requestId, Authentication authentication, ServerHttpResponse response) {
        return zorthProcessorService.findAllProcessorOutByRequestId(requestId, authentication)
                .map(processorOutEntityMapper::toDTO);
    }
}
