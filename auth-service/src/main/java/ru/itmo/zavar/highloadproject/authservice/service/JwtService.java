package ru.itmo.zavar.highloadproject.authservice.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUserName(String token);

    String generateToken(UserDetails userDetails);
}
