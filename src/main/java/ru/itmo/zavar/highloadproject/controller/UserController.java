package ru.itmo.zavar.highloadproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.dto.request.ChangeRoleRequest;
import ru.itmo.zavar.highloadproject.dto.request.SignUpRequest;
import ru.itmo.zavar.highloadproject.dto.response.MessageResponse;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.repo.UserRepository;
import ru.itmo.zavar.highloadproject.security.Role;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@Valid @RequestBody SignUpRequest request) {
        try {
            authenticationService.addUser(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/changeRole")
    public ResponseEntity<?> changeRole(@Valid @RequestBody ChangeRoleRequest request) {
        try {
            Optional<UserEntity> byUsername = userRepository.findByUsername(request.username());
            if (byUsername.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
            } else {
                UserEntity userEntity = byUsername.get();
                userEntity.setRole(Role.valueOf(request.role()));
                userRepository.save(userEntity);
                return ResponseEntity.ok().build();
            }
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
