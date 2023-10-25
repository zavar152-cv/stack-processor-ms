package ru.itmo.zavar.highloadproject.service;

import ru.itmo.zavar.highloadproject.dto.request.SignInRequest;
import ru.itmo.zavar.highloadproject.dto.request.SignUpRequest;

public interface AuthenticationService {
    void addUser(String username, String password);

    String signIn(String username, String password);
}
