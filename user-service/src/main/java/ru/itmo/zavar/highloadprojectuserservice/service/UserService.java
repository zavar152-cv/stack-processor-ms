package ru.itmo.zavar.highloadprojectuserservice.service;

import ru.itmo.zavar.highloadprojectuserservice.entity.security.UserEntity;

import java.util.Optional;

public interface UserService {
    UserEntity saveUser(UserEntity userEntity);

    void addUser(String username, String password);

    void changeRole(String username, String role);

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByUsername(String username);
}
