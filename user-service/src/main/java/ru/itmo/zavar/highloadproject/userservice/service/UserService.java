package ru.itmo.zavar.highloadproject.userservice.service;

import ru.itmo.zavar.highloadproject.userservice.entity.security.UserEntity;

import java.util.Optional;

public interface UserService {
    void addUser(String username, String password);

    void changeRole(String username, String role);

    UserEntity saveUser(UserEntity userEntity);

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByUsername(String username);
}
