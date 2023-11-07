package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highloadproject.clients.UserServiceClient;
import ru.itmo.zavar.highloadproject.dto.inner.response.UserServiceResponse;
import ru.itmo.zavar.highloadproject.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;
import ru.itmo.zavar.highloadproject.service.JwtService;
import ru.itmo.zavar.highloadproject.service.RoleService;
import ru.itmo.zavar.highloadproject.util.RoleConstants;

import javax.annotation.PostConstruct;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserServiceClient userServiceClient;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserEntityMapper userEntityMapper;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    @Transactional
    public void init() {
        // TODO: Перенести куда-то, чтобы не ломало регистрацию в Eureka (в идеале вообще избежать обращений к сторонним сервисам)
        /*
        if (roleService.getVipRole().isEmpty())
            roleService.saveRole(new RoleEntity(1L, RoleConstants.VIP));
        if (roleService.getUserRole().isEmpty())
            roleService.saveRole(new RoleEntity(2L, RoleConstants.USER));
        Optional<RoleEntity> optionalRoleEntity = roleService.getAdminRole();
        RoleEntity adminRole = optionalRoleEntity.orElseGet(() -> roleService.saveRole(new RoleEntity(0L, RoleConstants.ADMIN)));
        ResponseEntity<UserServiceResponse> response = userServiceClient.getByUsername("admin");
        if (response.getStatusCode().isError()) {
            UserEntity admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole)).build();
            userServiceClient.save(userEntityMapper.toRequest(admin));
        }
         */
    }

    @Override
    public void addUser(String username, String password) throws IllegalArgumentException {
        Optional<RoleEntity> roleUser = roleService.getUserRole();
        if (roleUser.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }
        var user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(roleUser.get())).build();
        ResponseEntity<UserServiceResponse> response = userServiceClient.getByUsername(user.getUsername());
        if (response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("User exists");
        } else {
            userServiceClient.save(userEntityMapper.toRequest(user));
        }
    }

    @Override
    public String signIn(String username, String password) throws IllegalArgumentException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        ResponseEntity<UserServiceResponse> response = userServiceClient.getByUsername(username);
        if (response.getStatusCode().isError()) {
            throw new IllegalArgumentException("User not found");
        }
        return jwtService.generateToken(userEntityMapper.fromResponse(response.getBody()));
    }

    @Override
    public void changeRole(String username, String role) {
        ResponseEntity<UserServiceResponse> response = userServiceClient.getByUsername(username);
        if (response.getStatusCode().isError()) {
            throw new NoSuchElementException();
        }
        UserEntity userEntity = userEntityMapper.fromResponse(response.getBody());
        Optional<RoleEntity> optionalRoleEntity = roleService.findByName(role);
        RoleEntity roleEntity = optionalRoleEntity.orElseThrow();
        userEntity.getRoles().clear();
        userEntity.getRoles().add(roleEntity);
        userServiceClient.save(userEntityMapper.toRequest(userEntity));
    }
}
