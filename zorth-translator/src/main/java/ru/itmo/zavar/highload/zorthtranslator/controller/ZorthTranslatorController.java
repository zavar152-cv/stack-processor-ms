package ru.itmo.zavar.highload.zorthtranslator.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.CompileResponse;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.request.CompileRequest;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.GetCompilerOutResponse;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.GetDebugMessagesResponse;
import ru.itmo.zavar.highload.zorthtranslator.mapper.CompilerOutEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.mapper.DebugMessagesEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.mapper.RequestEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highload.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highload.zorthtranslator.service.ZorthTranslatorService;
import ru.itmo.zavar.highload.zorthtranslator.util.RoleConstants;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class ZorthTranslatorController {
    private final ZorthTranslatorService zorthTranslatorService;
    private final CompilerOutService compilerOutService;
    private final DebugMessagesService debugMessagesService;

    private final DebugMessagesEntityMapper debugMessagesEntityMapper;
    private final CompilerOutEntityMapper compilerOutEntityMapper;
    private final RequestEntityMapper requestEntityMapper;


    @PostMapping("/compile")
    public Mono<CompileResponse> compile(@Valid @RequestBody CompileRequest compileRequest, Authentication authentication) {
        return zorthTranslatorService.compileAndLinkage(compileRequest.debug(), compileRequest.text(), authentication.getName())
                .map(requestEntityMapper::toDTO);
    }

    @GetMapping("/compiler-outs")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public Mono<Page<GetCompilerOutResponse>> getAllCompilerOut(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        return compilerOutService.findAllPageable(offset, limit)
                .map(page -> page.map(compilerOutEntityMapper::toDTO));
    }

    @GetMapping(value = "/compiler-outs", params = "request-id")
    @PreAuthorize("@zorthTranslatorServiceImpl.checkRequestOwnedByUser(authentication, #requestId)")
    public Mono<GetCompilerOutResponse> getCompilerOutOfRequest(@RequestParam("request-id") Long requestId) {
        return compilerOutService.findByRequestId(requestId)
                .map(compilerOutEntityMapper::toDTO)
                .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @GetMapping("/compiler-outs/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public Mono<GetCompilerOutResponse> getCompilerOut(@PathVariable Long id) {
        return compilerOutService.findById(id)
                .map(compilerOutEntityMapper::toDTO)
                .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @GetMapping("/debug-messages")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public Mono<Page<GetDebugMessagesResponse>> getAllDebugMessages(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                    @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        return debugMessagesService.findAllPageable(offset, limit)
                .map(page -> page.map(debugMessagesEntityMapper::toDTO));
    }

    @GetMapping(value = "/debug-messages", params = "request-id")
    @PreAuthorize("@zorthTranslatorServiceImpl.checkRequestOwnedByUser(authentication, #requestId)")
    public Mono<GetDebugMessagesResponse> getDebugMessagesOfRequest(@RequestParam("request-id") Long requestId) {
        return debugMessagesService.findByRequestId(requestId)
                .map(debugMessagesEntityMapper::toDTO)
                .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @GetMapping("/debug-messages/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public Mono<GetDebugMessagesResponse> getDebugMessages(@PathVariable Long id) {
        return debugMessagesService.findById(id)
                .map(debugMessagesEntityMapper::toDTO)
                .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }
}
