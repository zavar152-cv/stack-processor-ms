package ru.itmo.zavar.highloadproject.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.userservice.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.userservice.util.RoleConstants;
import ru.itmo.zavar.highloadproject.userservice.dto.inner.UserDTO;
import ru.itmo.zavar.highloadproject.userservice.dto.outer.request.AddUserRequestDTO;
import ru.itmo.zavar.highloadproject.userservice.dto.outer.request.ChangeRoleRequestDTO;
import ru.itmo.zavar.highloadproject.userservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadproject.userservice.service.UserService;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserServiceController {
    private final UserService userService;
    private final UserEntityMapper userEntityMapper;

    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@Valid @RequestBody AddUserRequestDTO dto) {
        try {
            userService.addUser(dto.username(), dto.password());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @PostMapping("/changeRole")
    public ResponseEntity<?> changeRoleOfUser(@Valid @RequestBody ChangeRoleRequestDTO dto) {
        try {
            userService.changeRole(dto.username(), dto.role());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<UserDTO> saveUser(@Valid @RequestBody UserDTO dto) {
        try {
            UserEntity userEntity = userEntityMapper.fromDTO(dto);
            dto = userEntityMapper.toDTO(userService.saveUser(userEntity));
            return ResponseEntity.ok(dto);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't save user");
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable Long id) {
        Optional<UserEntity> userEntity = userService.findById(id);
        if (userEntity.isPresent()) {
            UserDTO dto = userEntityMapper.toDTO(userEntity.get());
            return ResponseEntity.ok(dto);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @GetMapping("/getByUsername/{username}")
    public ResponseEntity<UserDTO> findUserByUsername(@PathVariable String username) {
        Optional<UserEntity> userEntity = userService.findByUsername(username);
        if (userEntity.isPresent()) {
            UserDTO dto = userEntityMapper.toDTO(userEntity.get());
            return ResponseEntity.ok(dto);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
