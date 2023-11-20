package ru.itmo.zavar.highload.authservice.service.impl;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highload.authservice.client.UserServiceClient;
import ru.itmo.zavar.highload.authservice.dto.inner.UserDTO;
import ru.itmo.zavar.highload.authservice.entity.security.UserEntity;
import ru.itmo.zavar.highload.authservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highload.authservice.service.AuthenticationService;
import ru.itmo.zavar.highload.authservice.service.JwtService;

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
        ResponseEntity<UserDTO> response = userServiceClient.findUserByUsername(username);
        return jwtService.generateToken(userEntityMapper.fromDTO(response.getBody()));
    }

    @Override
    public UserEntity validateToken(String jwtToken) throws JwtException, IllegalArgumentException {
        String username = jwtService.extractUserName(jwtToken);
        ResponseEntity<UserDTO> response = userServiceClient.findUserByUsername(username);
        return userEntityMapper.fromDTO(response.getBody());
    }

}
