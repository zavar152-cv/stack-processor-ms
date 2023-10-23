package ru.itmo.zavar.highloadproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.dto.request.CompileRequest;
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
    public ResponseEntity<?> compile(@RequestBody CompileRequest compileRequest) {
        try {
            zorthTranslatorService.compileAndLinkage(compileRequest.debug(), compileRequest.text());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }


    @GetMapping("/getDebugMessages")
    public ResponseEntity<?> getDebugMessages() {
        return ResponseEntity.of(Optional.ofNullable(zorthTranslatorService.getDebugMessages()));
    }
}
