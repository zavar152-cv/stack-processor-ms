package ru.itmo.zavar.highloadproject.zorthtranslator.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.RequestDTO;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.request.CompileRequest;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetAllCompilerOutResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetAllDebugMessagesResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetCompilerOutResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetDebugMessagesResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.mapper.CompilerOutEntityMapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.mapper.DebugMessagesEntityMapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.mapper.RequestEntityMapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.ZorthTranslatorService;
import ru.itmo.zavar.highloadproject.zorthtranslator.util.RoleConstants;

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
    public ResponseEntity<RequestDTO> compile(@Valid @RequestBody CompileRequest compileRequest, Authentication authentication) {
        try {
            RequestEntity requestEntity = zorthTranslatorService.compileAndLinkage(compileRequest.debug(), compileRequest.text(), authentication.getName());
            return ResponseEntity.ok(requestEntityMapper.toDTO(requestEntity));
        } catch (ZorthException | NoSuchElementException | DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/compiler-outs")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<Page<GetAllCompilerOutResponse>> getAllCompilerOut(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                             @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        Page<CompilerOutEntity> all = compilerOutService.findAllPageable(offset, limit);
        Page<GetAllCompilerOutResponse> response = all.map(compilerOutEntityMapper::toDetailedDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/compiler-outs", params = "request-id")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "') || (hasRole('" + RoleConstants.VIP + "') && @zorthTranslatorServiceImpl.checkRequestOwnedByUser(authentication.name, #requestId))")
    public ResponseEntity<GetCompilerOutResponse> getCompilerOutOfRequest(@RequestParam("request-id") Long requestId) {
        try {
            CompilerOutEntity compilerOutEntity = compilerOutService.findByRequestId(requestId);
            return ResponseEntity.ok(compilerOutEntityMapper.toDTO(compilerOutEntity));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/compiler-outs/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<GetCompilerOutResponse> getCompilerOut(@PathVariable Long id) {
        try {
            CompilerOutEntity compilerOutEntity = compilerOutService.findById(id);
            return ResponseEntity.ok(compilerOutEntityMapper.toDTO(compilerOutEntity));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @GetMapping("/debug-messages")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<Page<GetAllDebugMessagesResponse>> getAllDebugMessages(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                                 @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        Page<DebugMessagesEntity> all = debugMessagesService.findAllPageable(offset, limit);
        Page<GetAllDebugMessagesResponse> response = all.map(debugMessagesEntityMapper::toDetailedDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/debug-messages", params = "request-id")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "') || (hasRole('" + RoleConstants.VIP + "') && @zorthTranslatorServiceImpl.checkRequestOwnedByUser(authentication.name, #requestId))")
    public ResponseEntity<GetDebugMessagesResponse> getDebugMessagesOfRequest(@RequestParam("request-id") Long requestId) {
        try {
            DebugMessagesEntity debugMessagesEntity = debugMessagesService.findByRequestId(requestId);
            return ResponseEntity.ok(debugMessagesEntityMapper.toDTO(debugMessagesEntity));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/debug-messages/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<GetDebugMessagesResponse> getDebugMessages(@PathVariable Long id) {
        try {
            DebugMessagesEntity debugMessagesEntity = debugMessagesService.findById(id);
            return ResponseEntity.ok(debugMessagesEntityMapper.toDTO(debugMessagesEntity));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
