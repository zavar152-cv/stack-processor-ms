package ru.itmo.zavar.highloadprojectauthservice.service;

import ru.itmo.zavar.highloadprojectauthservice.entity.security.UserEntity;

public interface AuthenticationService {
    String signIn(String username, String password);

    UserEntity validateToken(String jwtToken);
}
