package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.dto.request.SignInRequest;
import ru.itmo.zavar.highloadproject.dto.request.SignUpRequest;
import ru.itmo.zavar.highloadproject.dto.response.JwtAuthenticationResponse;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.repo.UserRepository;
import ru.itmo.zavar.highloadproject.security.Role;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;
import ru.itmo.zavar.highloadproject.service.JwtService;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public JwtAuthenticationResponse signup(SignUpRequest request) {
        var user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.User).build();
        if(userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User exists");
        } else {
            userRepository.save(user);
            var jwt = jwtService.generateToken(user);
            return JwtAuthenticationResponse.builder().token(jwt).build();
        }
    }

    @Override
    public JwtAuthenticationResponse signin(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }
}
