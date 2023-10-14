package ru.itmo.zavar.highloadproject.service;

import ru.itmo.zavar.highloadproject.dto.request.SignInRequest;
import ru.itmo.zavar.highloadproject.dto.request.SignUpRequest;

public interface AuthenticationService {
    void addUser(SignUpRequest request);

    String signIn(SignInRequest request);
}
