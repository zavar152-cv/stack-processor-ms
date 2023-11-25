package ru.itmo.zavar.highload.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highload.userservice.dto.inner.RoleDTO;
import ru.itmo.zavar.highload.userservice.dto.inner.UserDTO;
import ru.itmo.zavar.highload.userservice.dto.outer.request.AddUserRequest;
import ru.itmo.zavar.highload.userservice.dto.outer.request.ChangeRoleRequest;
import ru.itmo.zavar.highload.userservice.dto.outer.response.GetRequestsResponse;
import ru.itmo.zavar.highload.userservice.entity.security.RoleEntity;
import ru.itmo.zavar.highload.userservice.entity.security.UserEntity;
import ru.itmo.zavar.highload.userservice.mapper.RequestEntityMapper;
import ru.itmo.zavar.highload.userservice.mapper.RoleEntityMapper;
import ru.itmo.zavar.highload.userservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highload.userservice.service.UserService;
import ru.itmo.zavar.highload.userservice.util.RoleConstants;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class UserServiceController {
    private final UserService userService;
    private final UserEntityMapper userEntityMapper;
    private final RoleEntityMapper roleEntityMapper;
    private final RequestEntityMapper requestEntityMapper;

    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @PostMapping("/users")
    public ResponseEntity<?> addUser(@Valid @RequestBody AddUserRequest request) {
        try {
            userService.addUser(request.username(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @PutMapping("/users/{username}/roles")
    public ResponseEntity<?> changeRoleOfUser(@PathVariable String username, @Valid @RequestBody ChangeRoleRequest request) {
        try {
            userService.changeRole(username, request.role());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "') || (hasRole('" + RoleConstants.VIP + "') && (#username == authentication.name))")
    @GetMapping("/users/{username}/requests")
    public ResponseEntity<List<GetRequestsResponse>> getRequestsOfUser(@PathVariable String username) {
        try {
            UserEntity userEntity = userService.findUserByUsername(username);
            List<GetRequestsResponse> requests = userEntity.getRequests()
                    .stream()
                    .map(requestEntityMapper::toDTO)
                    .toList();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Requests-Count", String.valueOf(requests.size()));
            return ResponseEntity.ok().headers(responseHeaders).body(requests);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/users")
    public ResponseEntity<?> saveUser(@Valid @RequestBody UserDTO dto) {
        userEntityMapper.toDTO(userService.saveUser(userEntityMapper.fromDTO(dto)));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String username) {
        try {
            UserEntity userEntity = userService.findUserByUsername(username);
            return ResponseEntity.ok(userEntityMapper.toDTO(userEntity));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/roles/{name}")
    public ResponseEntity<RoleDTO> getRole(@PathVariable String name) {
        try {
            RoleEntity roleEntity = userService.findRoleByName(name);
            return ResponseEntity.ok(roleEntityMapper.toDTO(roleEntity));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
