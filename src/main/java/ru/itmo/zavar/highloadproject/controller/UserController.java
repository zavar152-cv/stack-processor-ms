package ru.itmo.zavar.highloadproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.zavar.highloadproject.dto.request.ChangeRoleRequest;
import ru.itmo.zavar.highloadproject.dto.request.SignUpRequest;
import ru.itmo.zavar.highloadproject.dto.response.MessageResponse;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.repo.UserRepository;
import ru.itmo.zavar.highloadproject.security.Role;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestBody SignUpRequest request) {
        try {
            authenticationService.addUser(request);
            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(new MessageResponse(exception.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/changeRole")
    public ResponseEntity<?> changeRole(@RequestBody ChangeRoleRequest request) {
        try {
            Optional<UserEntity> byUsername = userRepository.findByUsername(request.username());
            if(byUsername.isEmpty()) {
                return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(new MessageResponse("User not found"));
            } else {
                UserEntity userEntity = byUsername.get();
                userEntity.setRole(Role.valueOf(request.role()));
                userRepository.save(userEntity);
                return ResponseEntity.ok().build();
            }
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(new MessageResponse(exception.getMessage()));
        }
    }
}
