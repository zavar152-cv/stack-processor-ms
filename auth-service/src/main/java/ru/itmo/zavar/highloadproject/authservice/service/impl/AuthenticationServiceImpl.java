package ru.itmo.zavar.highloadproject.authservice.service.impl;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.authservice.client.UserServiceClient;
import ru.itmo.zavar.highloadproject.authservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadproject.authservice.service.AuthenticationService;
import ru.itmo.zavar.highloadproject.authservice.service.JwtService;
import ru.itmo.zavar.highloadproject.authservice.dto.inner.response.UserEntityResponse;
import ru.itmo.zavar.highloadproject.authservice.entity.security.UserEntity;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;
    private final UserEntityMapper userEntityMapper;
    private final AuthenticationManager authenticationManager;

    @Override
    public String signIn(String username, String password) throws AuthenticationException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        ResponseEntity<UserEntityResponse> response = userServiceClient.getByUsername(username);
        return jwtService.generateToken(userEntityMapper.fromResponse(response.getBody()));
    }

    @Override
    public UserEntity validateToken(String jwtToken) throws JwtException, IllegalArgumentException {
        String username = jwtService.extractUserName(jwtToken);
        ResponseEntity<UserEntityResponse> response = userServiceClient.getByUsername(username);
        return userEntityMapper.fromResponse(response.getBody());
    }

}
