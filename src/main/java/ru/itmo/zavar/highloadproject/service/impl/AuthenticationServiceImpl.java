package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
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

    @PostConstruct
    @Transactional
    public void init() {
        if(roleRepository.findByName("ROLE_VIP").isEmpty())
            roleRepository.save(new RoleEntity(1L, "ROLE_VIP"));
        if(roleRepository.findByName("ROLE_USER").isEmpty())
            roleRepository.save(new RoleEntity(2L, "ROLE_USER"));
        if(roleRepository.findByName("ROLE_ADMIN").isEmpty())
            roleRepository.save(new RoleEntity(0L, "ROLE_ADMIN"));
        if(userRepository.findByUsername("admin").isEmpty()) {
            Optional<RoleEntity> roleAdmin = roleRepository.findByName("ROLE_ADMIN");
            if(roleAdmin.isEmpty()) {
                throw new IllegalArgumentException("Role not found");
            }
            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .roles(Set.of(roleAdmin.get())).build();
            userRepository.save(admin);
        }
    }

    @Override
    public void addUser(SignUpRequest request) throws IllegalArgumentException {
        Optional<RoleEntity> roleUser = roleRepository.findByName("ROLE_USER");
        if(roleUser.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }
        var user = UserEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(roleUser.get())).build();
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User exists");
        } else {
            userRepository.save(user);
        }
    }

    @Override
    public String signIn(SignInRequest request) throws IllegalArgumentException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jwtService.generateToken(user);
    }
}
