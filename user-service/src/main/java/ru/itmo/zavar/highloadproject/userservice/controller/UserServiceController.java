package ru.itmo.zavar.highloadproject.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.userservice.dto.inner.UserDTO;
import ru.itmo.zavar.highloadproject.userservice.dto.outer.request.AddUserRequest;
import ru.itmo.zavar.highloadproject.userservice.dto.outer.request.ChangeRoleRequest;
import ru.itmo.zavar.highloadproject.userservice.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.userservice.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.userservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadproject.userservice.service.UserService;
import ru.itmo.zavar.highloadproject.userservice.util.RoleConstants;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserServiceController {
    private final UserService userService;
    private final UserEntityMapper userEntityMapper;

    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @PostMapping
    public ResponseEntity<?> addUser(@Valid @RequestBody AddUserRequest request) {
        try {
            userService.addUser(request.username(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @PutMapping("/{username}/roles")
    public ResponseEntity<?> changeRoleOfUser(@PathVariable String username, @Valid @RequestBody ChangeRoleRequest request) {
        try {
            userService.changeRole(username, request.role());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "') || (hasRole('" + RoleConstants.VIP + "') && (#username == authentication.name))")
    @GetMapping("/{username}/requests")
    public ResponseEntity<List<RequestEntity>> getRequestsOfUser(@PathVariable String username) {
        try {
            UserEntity userEntity = userService.findUserByUsername(username);
            List<RequestEntity> requests = userEntity.getRequests();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Requests-Count", String.valueOf(requests.size()));
            return ResponseEntity.ok().headers(responseHeaders).body(requests);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> saveUser(@Valid @RequestBody UserDTO dto) {
        try {
            userEntityMapper.toDTO(userService.saveUser(userEntityMapper.fromDTO(dto)));
            return ResponseEntity.ok().build();
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't save user");
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> findUserByUsername(@PathVariable String username) {
        try {
            UserEntity userEntity = userService.findUserByUsername(username);
            return ResponseEntity.ok(userEntityMapper.toDTO(userEntity));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
