package ru.itmo.zavar.highload.authservice.service;

import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUserName(String token) throws JwtException, IllegalArgumentException;

    String generateToken(UserDetails userDetails);
}
