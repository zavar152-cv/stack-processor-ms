package ru.itmo.zavar.highloadprojectuserservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.request.UserEntityRequest;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.response.UserEntityResponse;
import ru.itmo.zavar.highloadprojectuserservice.dto.outer.request.AddUserRequest;
import ru.itmo.zavar.highloadprojectuserservice.dto.outer.request.ChangeRoleRequest;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.UserEntity;
import ru.itmo.zavar.highloadprojectuserservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadprojectuserservice.service.UserService;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserServiceController {
    private final UserService userService;
    private final UserEntityMapper userEntityMapper;

    @PostMapping("/save")
    public ResponseEntity<UserEntityResponse> save(@Valid @RequestBody UserEntityRequest request) {
        try {
            UserEntity userEntity = userEntityMapper.fromRequest(request);
            UserEntityResponse response = userEntityMapper.toResponse(userService.saveUser(userEntity));
            return ResponseEntity.ok(response);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't save user");
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> add(@Valid @RequestBody AddUserRequest request) {
        try {
            userService.addUser(request.username(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/changeRole")
    public ResponseEntity<?> changeRole(@Valid @RequestBody ChangeRoleRequest request) {
        try {
            userService.changeRole(request.username(), request.role());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<UserEntityResponse> getById(@PathVariable Long id) {
        Optional<UserEntity> userEntity = userService.findById(id);
        if (userEntity.isPresent()) {
            UserEntityResponse response = userEntityMapper.toResponse(userEntity.get());
            return ResponseEntity.ok(response);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @GetMapping("/getByUsername/{username}")
    public ResponseEntity<UserEntityResponse> getByUsername(@PathVariable String username) {
        Optional<UserEntity> userEntity = userService.findByUsername(username);
        if (userEntity.isPresent()) {
            UserEntityResponse response = userEntityMapper.toResponse(userEntity.get());
            return ResponseEntity.ok(response);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
