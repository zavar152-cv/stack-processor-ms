package ru.itmo.zavar.highloadproject.userservice.service;

import org.springframework.dao.DataAccessException;
import ru.itmo.zavar.highloadproject.userservice.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.userservice.entity.security.UserEntity;

import java.util.NoSuchElementException;

public interface UserService {
    void addUser(String username, String password) throws NoSuchElementException, IllegalArgumentException, DataAccessException;

    void changeRole(String username, String role) throws NoSuchElementException, DataAccessException;

    UserEntity saveUser(UserEntity userEntity) throws DataAccessException;

    UserEntity findUserByUsername(String username) throws NoSuchElementException;

    RoleEntity saveRole(RoleEntity roleEntity) throws DataAccessException;

    RoleEntity findRoleByName(String name) throws NoSuchElementException;
}
