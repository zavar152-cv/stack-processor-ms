package ru.itmo.zavar.highload.authservice.service;

import io.jsonwebtoken.JwtException;
import org.springframework.security.core.AuthenticationException;
import ru.itmo.zavar.highload.authservice.entity.security.UserEntity;

public interface AuthenticationService {
    String signIn(String username, String password) throws AuthenticationException;

    UserEntity validateToken(String jwtToken) throws JwtException, IllegalArgumentException;
}
