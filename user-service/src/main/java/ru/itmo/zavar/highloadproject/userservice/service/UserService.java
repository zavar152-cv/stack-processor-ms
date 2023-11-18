package ru.itmo.zavar.highloadproject.userservice.service;

import org.springframework.dao.DataAccessException;
import ru.itmo.zavar.highloadproject.userservice.entity.security.UserEntity;

public interface UserService {
    void addUser(String username, String password) throws IllegalArgumentException, DataAccessException;

    void changeRole(String username, String role) throws IllegalArgumentException, DataAccessException;

    UserEntity saveUser(UserEntity userEntity) throws DataAccessException;

    UserEntity findUserByUsername(String username) throws IllegalArgumentException;
}
