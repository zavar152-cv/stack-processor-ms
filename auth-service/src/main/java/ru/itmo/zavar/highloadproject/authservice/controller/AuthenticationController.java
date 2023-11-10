package ru.itmo.zavar.highloadproject.authservice.controller;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.authservice.dto.inner.request.ValidateTokenRequest;
import ru.itmo.zavar.highloadproject.authservice.dto.inner.response.ValidateTokenResponse;
import ru.itmo.zavar.highloadproject.authservice.dto.outer.request.SignInRequest;
import ru.itmo.zavar.highloadproject.authservice.dto.outer.response.SignInResponse;
import ru.itmo.zavar.highloadproject.authservice.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.authservice.service.AuthenticationService;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/signIn")
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInRequest request) {
        try {
            SignInResponse response = SignInResponse.builder()
                    .token(authenticationService.signIn(request.username(), request.password()))
                    .build();
            return ResponseEntity.ok(response);
        } catch (AuthenticationException exception) {
            if (exception.getCause() instanceof ResponseStatusException) {
                throw (ResponseStatusException) exception.getCause();
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }

    @PostMapping("/validateToken")
    public ResponseEntity<ValidateTokenResponse> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
        try {
            UserEntity user = authenticationService.validateToken(request.jwtToken());
            return ResponseEntity.ok(ValidateTokenResponse.builder()
                    .username(user.getUsername())
                    .authorities(new ArrayList<>(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()))
                    .build());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }
}
