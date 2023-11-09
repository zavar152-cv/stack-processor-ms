package ru.itmo.zavar.highloadprojectauthservice.service.impl;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadprojectauthservice.client.UserServiceClient;
import ru.itmo.zavar.highloadprojectauthservice.dto.inner.response.UserEntityResponse;
import ru.itmo.zavar.highloadprojectauthservice.entity.security.UserEntity;
import ru.itmo.zavar.highloadprojectauthservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadprojectauthservice.service.AuthenticationService;
import ru.itmo.zavar.highloadprojectauthservice.service.JwtService;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;
    private final UserEntityMapper userEntityMapper;
    private final AuthenticationManager authenticationManager;

    @Override
    public String signIn(String username, String password) throws IllegalArgumentException, AuthenticationException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        ResponseEntity<UserEntityResponse> response = userServiceClient.getByUsername(username);
        if (response.getStatusCode().isError()) {
            throw new IllegalArgumentException("User not found");
        }
        return jwtService.generateToken(userEntityMapper.fromResponse(response.getBody()));
    }

    @Override
    public UserEntity validateToken(String jwtToken) throws JwtException, IllegalArgumentException {
        String username = jwtService.extractUserName(jwtToken);
        ResponseEntity<UserEntityResponse> response = userServiceClient.getByUsername(username);
        if (response.getStatusCode().isError()) {
            throw new IllegalArgumentException("User not found");
        }
        return userEntityMapper.fromResponse(response.getBody());
    }

}
