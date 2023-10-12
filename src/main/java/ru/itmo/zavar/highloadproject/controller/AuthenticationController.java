package ru.itmo.zavar.highloadproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.zavar.highloadproject.dto.request.SignInRequest;
import ru.itmo.zavar.highloadproject.dto.request.SignUpRequest;
import ru.itmo.zavar.highloadproject.dto.response.JwtAuthenticationResponse;
import ru.itmo.zavar.highloadproject.dto.response.MessageResponse;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.signup(request));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(new MessageResponse(exception.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SignInRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.signin(request));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(new MessageResponse(exception.getMessage()));
        }
    }
}
