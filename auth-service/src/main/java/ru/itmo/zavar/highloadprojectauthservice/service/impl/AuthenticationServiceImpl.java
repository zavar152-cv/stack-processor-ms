package ru.itmo.zavar.highloadprojectauthservice.service.impl;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadprojectauthservice.entity.security.UserEntity;
import ru.itmo.zavar.highloadprojectauthservice.service.AuthenticationService;
import ru.itmo.zavar.highloadprojectauthservice.service.JwtService;
import ru.itmo.zavar.highloadprojectauthservice.service.UserService;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Override
    public String signIn(String username, String password) throws IllegalArgumentException, AuthenticationException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserEntity user = userService.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jwtService.generateToken(user);
    }

    @Override
    public UserEntity validateToken(String jwtToken) throws JwtException, IllegalArgumentException {
        String username = jwtService.extractUserName(jwtToken); // достаточно для проверки валидности токена
        return userService.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

}
