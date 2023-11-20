package ru.itmo.zavar.highload.authservice.service;

import ru.itmo.zavar.highload.authservice.entity.security.UserEntity;

public interface AuthenticationService {
    String signIn(String username, String password);

    UserEntity validateToken(String jwtToken);
}
