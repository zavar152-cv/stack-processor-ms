package ru.itmo.zavar.highload.zorthprocessor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> pipeline(@Valid @RequestBody PipelineRequest pipelineRequest) {
        /* TODO: call translator
        try {
            RequestEntity requestEntity = zorthTranslatorService.compileAndLinkage(pipelineRequest.debug(), pipelineRequest.text(), (UserEntity) authentication.getPrincipal());
            Optional<CompilerOutEntity> optionalCompilerOut = zorthTranslatorService.getCompilerOutputByRequestId(requestEntity.getId());
            if(optionalCompilerOut.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compiler out found");
            } else {
                CompilerOutEntity compilerOut = optionalCompilerOut.get();
                zorthProcessorService.startProcessorAndGetLogs(pipelineRequest.input(), compilerOut);
            }
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | ZorthException | IllegalArgumentException
                 | ControlUnitException | ParseException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
         */
        return ResponseEntity.ok().build();
    }

    @PostMapping("/execute")
    public Mono<ProcessorOutDTO> execute(@Valid @RequestBody ExecuteRequest executeRequest, Authentication authentication) {
        try {
            return zorthProcessorService.startProcessorAndGetLogs(executeRequest.input(), executeRequest.requestId(), authentication)
                    .map(processorOutEntityMapper::toDTO);
        } catch (ControlUnitException | ParseException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        } catch (ResponseStatusException e) {
            return Mono.error(new ResponseStatusException(e.getStatusCode(), e.getMessage()));
        }
    }

    @GetMapping(value = "/processor-outs", params = "request-id")
    public Flux<ProcessorOutDTO> getAllProcessorOut(@RequestParam("request-id") Long requestId, Authentication authentication, ServerHttpResponse response) {
        return zorthProcessorService.findAllProcessorOutByRequestId(requestId, authentication)
                .map(processorOutEntityMapper::toDTO);
    }
}
