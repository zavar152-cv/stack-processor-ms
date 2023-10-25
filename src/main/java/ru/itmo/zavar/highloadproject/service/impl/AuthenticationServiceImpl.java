package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highloadproject.dto.request.SignInRequest;
import ru.itmo.zavar.highloadproject.dto.request.SignUpRequest;
import ru.itmo.zavar.highloadproject.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.repo.RoleRepository;
import ru.itmo.zavar.highloadproject.repo.UserRepository;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;
import ru.itmo.zavar.highloadproject.service.JwtService;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    @Transactional
    public void init() {
        if(roleRepository.findByName("ROLE_VIP").isEmpty())
            roleRepository.save(new RoleEntity(1L, "ROLE_VIP"));
        if(roleRepository.findByName("ROLE_USER").isEmpty())
            roleRepository.save(new RoleEntity(2L, "ROLE_USER"));
        Optional<RoleEntity> optionalRoleEntity = roleRepository.findByName("ROLE_ADMIN");
        RoleEntity adminRole = optionalRoleEntity.orElseGet(() -> roleRepository.save(new RoleEntity(0L, "ROLE_ADMIN")));
        if(userRepository.findByUsername("admin").isEmpty()) {
            UserEntity admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole)).build();
            userRepository.save(admin);
        }
    }

    @Override
    public void addUser(String username, String password) throws IllegalArgumentException {
        Optional<RoleEntity> roleUser = roleRepository.findByName("ROLE_USER");
        if(roleUser.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }
        var user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(roleUser.get())).build();
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User exists");
        } else {
            userRepository.save(user);
        }
    }

    @Override
    public String signIn(String username, String password) throws IllegalArgumentException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jwtService.generateToken(user);
    }
}
