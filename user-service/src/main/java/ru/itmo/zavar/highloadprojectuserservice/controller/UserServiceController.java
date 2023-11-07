package ru.itmo.zavar.highloadprojectuserservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.request.UserServiceRequest;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.response.UserServiceResponse;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.UserEntity;
import ru.itmo.zavar.highloadprojectuserservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadprojectuserservice.service.UserService;


import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserServiceController {
    private final UserService userService;
    private final UserEntityMapper userEntityMapper;

    @PostMapping("/save")
    public ResponseEntity<UserServiceResponse> save(@Valid @RequestBody UserServiceRequest request) {
        try {
            UserEntity userEntity = userEntityMapper.fromRequest(request);
            UserServiceResponse response = userEntityMapper.toResponse(userService.saveUser(userEntity));
            return ResponseEntity.ok(response);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't save user");
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<UserServiceResponse> getById(@PathVariable Long id) {
        Optional<UserEntity> userEntity = userService.findById(id);
        if (userEntity.isPresent()) {
            UserServiceResponse response = userEntityMapper.toResponse(userEntity.get());
            return ResponseEntity.ok(response);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @GetMapping("/getByUsername/{username}")
    public ResponseEntity<UserServiceResponse> getByUsername(@PathVariable String username) {
        Optional<UserEntity> userEntity = userService.findByUsername(username);
        if (userEntity.isPresent()) {
            UserServiceResponse response = userEntityMapper.toResponse(userEntity.get());
            return ResponseEntity.ok(response);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
