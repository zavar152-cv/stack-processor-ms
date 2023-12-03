package ru.itmo.zavar.highload.zorthprocessor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.ExecuteRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.PipelineRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.response.GetProcessorOutResponse;
import ru.itmo.zavar.highload.zorthprocessor.mapper.ProcessorOutEntityMapper;
import ru.itmo.zavar.highload.zorthprocessor.service.ZorthProcessorService;

@RestController
@RequiredArgsConstructor
public class ZorthProcessorController {
    private final ZorthProcessorService zorthProcessorService;
    private final ProcessorOutEntityMapper processorOutEntityMapper;

    @PostMapping("/pipeline")
    public Mono<GetProcessorOutResponse> pipeline(@Valid @RequestBody PipelineRequest request, Authentication authentication) {
        return zorthProcessorService.pipeline(request.debug(), request.text(), request.input(), authentication)
                .map(processorOutEntityMapper::toDTO);
    }

    @PostMapping("/execute")
    public Mono<GetProcessorOutResponse> execute(@Valid @RequestBody ExecuteRequest request, Authentication authentication) {
        return zorthProcessorService.startProcessorAndGetLogs(request.input(), request.requestId(), authentication)
                .map(processorOutEntityMapper::toDTO);
    }

    @GetMapping(value = "/processor-outs", params = "request-id")
    public Flux<GetProcessorOutResponse> getProcessorOutOfRequest(@RequestParam("request-id") Long requestId, Authentication authentication) {
        return zorthProcessorService.findAllProcessorOutByRequestId(requestId, authentication)
                .map(processorOutEntityMapper::toDTO);
    }
}
