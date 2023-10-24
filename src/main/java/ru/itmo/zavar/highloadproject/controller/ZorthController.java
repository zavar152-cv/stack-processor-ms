package ru.itmo.zavar.highloadproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.dto.request.CompileRequest;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.repo.CompilerOutRepository;
import ru.itmo.zavar.highloadproject.repo.DebugMessagesRepository;
import ru.itmo.zavar.highloadproject.repo.ProcessorOutRepository;
import ru.itmo.zavar.highloadproject.repo.RequestRepository;
import ru.itmo.zavar.highloadproject.service.ZorthTranslatorService;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/zorth")
@RequiredArgsConstructor
public class ZorthController {

    private final ZorthTranslatorService zorthTranslatorService;
    @PostMapping("/compile")
    public ResponseEntity<?> compile(@RequestBody CompileRequest compileRequest, Authentication authentication) {
        try {
            zorthTranslatorService.compileAndLinkage(compileRequest.debug(), compileRequest.text(), (UserEntity) authentication.getPrincipal());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }


    @GetMapping("/getDebugMessages/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getDebugMessages(@PathVariable Long id) {
        return ResponseEntity.of(zorthTranslatorService.getDebugMessages(id));
    }

    @GetMapping("/getCompilerOut/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getCompilerOut(@PathVariable Long id) {
        return ResponseEntity.of(zorthTranslatorService.getCompilerOutput(id));
    }

    @GetMapping("/getAllRequests/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllRequestsByUserId(@PathVariable Long id) {
        return ResponseEntity.of(Optional.ofNullable(zorthTranslatorService.getAllRequestsByUserId(id)));
    }
}
