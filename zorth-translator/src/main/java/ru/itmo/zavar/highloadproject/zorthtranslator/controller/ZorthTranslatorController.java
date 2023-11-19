package ru.itmo.zavar.highloadproject.zorthtranslator.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.inner.RequestDTO;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.request.CompileRequest;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetAllCompilerOutResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetAllDebugMessagesResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetCompilerOutResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetDebugMessagesResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.ZorthTranslatorService;
import ru.itmo.zavar.highloadproject.zorthtranslator.util.RoleConstants;
import ru.itmo.zavar.highloadproject.zorthtranslator.util.ZorthUtil;

import java.util.ArrayList;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class ZorthTranslatorController {
    private final ZorthTranslatorService zorthTranslatorService;
    private final CompilerOutService compilerOutService;
    private final DebugMessagesService debugMessagesService;

    @PostMapping("/compile")
    public ResponseEntity<RequestDTO> compile(@Valid @RequestBody CompileRequest compileRequest, Authentication authentication) {
        try {
            zorthTranslatorService.compileAndLinkage(compileRequest.debug(), compileRequest.text(), authentication.getName());
            return ResponseEntity.ok().build();
        } catch (ZorthException | NoSuchElementException | DataAccessException | ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/compiler-outs")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<Page<GetAllCompilerOutResponse>> getAllCompilerOut(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                             @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        ArrayList<GetAllCompilerOutResponse> list = new ArrayList<>();
        compilerOutService.findAllPageable(offset, limit).forEach(compilerOutEntity -> {
            ArrayList<Long> program = new ArrayList<>();
            ArrayList<Long> data = new ArrayList<>();
            ZorthUtil.fromByteArrayToLongList(program, compilerOutEntity.getProgram());
            ZorthUtil.fromByteArrayToLongList(data, compilerOutEntity.getData());
            list.add(GetAllCompilerOutResponse.builder()
                    .id(compilerOutEntity.getId())
                    .requestId(compilerOutEntity.getRequest().getId())
                    .program(program)
                    .data(data)
                    .build());
        });
        Page<GetAllCompilerOutResponse> page = new PageImpl<>(list);
        return ResponseEntity.ok(page);
    }

    @GetMapping(value = "/compiler-outs", params = "request-id")
    @PreAuthorize("hasRole('" + RoleConstants.VIP + "')")
    public ResponseEntity<GetCompilerOutResponse> getCompilerOutOfRequest(@RequestParam("request-id") Long requestId) {
        try {
            CompilerOutEntity compilerOutEntity = compilerOutService.findByRequestId(requestId);
            ArrayList<Long> program = new ArrayList<>();
            ArrayList<Long> data = new ArrayList<>();
            ZorthUtil.fromByteArrayToLongList(program, compilerOutEntity.getProgram());
            ZorthUtil.fromByteArrayToLongList(data, compilerOutEntity.getData());
            return ResponseEntity.ok(new GetCompilerOutResponse(compilerOutEntity.getId(), program, data));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/compiler-outs/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<GetCompilerOutResponse> getCompilerOut(@PathVariable Long id) {
        try {
            CompilerOutEntity compilerOutEntity = compilerOutService.findById(id);
            ArrayList<Long> program = new ArrayList<>();
            ArrayList<Long> data = new ArrayList<>();
            ZorthUtil.fromByteArrayToLongList(program, compilerOutEntity.getProgram());
            ZorthUtil.fromByteArrayToLongList(data, compilerOutEntity.getData());
            return ResponseEntity.ok(new GetCompilerOutResponse(compilerOutEntity.getId(), program, data));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @GetMapping("/debug-messages")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<Page<GetAllDebugMessagesResponse>> getAllDebugMessages(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                                 @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        Page<DebugMessagesEntity> all = debugMessagesService.findAllPageable(offset, limit);
        Page<GetAllDebugMessagesResponse> response = all.map(debugMessagesEntity -> GetAllDebugMessagesResponse.builder()
                .id(debugMessagesEntity.getId())
                .requestId(debugMessagesEntity.getRequest().getId())
                .text(debugMessagesEntity.getText().split("\n"))
                .build());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/debug-messages", params = "request-id")
    @PreAuthorize("hasRole('" + RoleConstants.VIP + "')")
    public ResponseEntity<GetDebugMessagesResponse> getDebugMessagesOfRequest(@RequestParam("request-id") Long requestId) {
        try {
            DebugMessagesEntity debugMessagesEntity = debugMessagesService.findByRequestId(requestId);
            return ResponseEntity.ok(GetDebugMessagesResponse.builder()
                    .id(debugMessagesEntity.getId())
                    .text(debugMessagesEntity.getText().split("\n"))
                    .build());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/debug-messages/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<GetDebugMessagesResponse> getDebugMessages(@PathVariable Long id) {
        try {
            DebugMessagesEntity debugMessages = debugMessagesService.findById(id);
            return ResponseEntity.ok(GetDebugMessagesResponse.builder()
                    .id(debugMessages.getId())
                    .text(debugMessages.getText().split("\n"))
                    .build());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
