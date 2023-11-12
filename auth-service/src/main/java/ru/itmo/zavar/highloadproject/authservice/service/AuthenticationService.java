package ru.itmo.zavar.highloadproject.authservice.service;

import ru.itmo.zavar.highloadproject.authservice.entity.security.UserEntity;

public interface AuthenticationService {
    String signIn(String username, String password);

    UserEntity validateToken(String jwtToken);
}
