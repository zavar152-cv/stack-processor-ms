package ru.itmo.zavar.highloadproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.dto.request.SignInRequest;
import ru.itmo.zavar.highloadproject.dto.response.JwtAuthenticationResponse;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/signIn")
    public ResponseEntity<JwtAuthenticationResponse> signIn(@Valid @RequestBody SignInRequest request) {
        try {
            JwtAuthenticationResponse response = JwtAuthenticationResponse.builder().token(authenticationService.signIn(request.username(), request.password())).build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }
}
