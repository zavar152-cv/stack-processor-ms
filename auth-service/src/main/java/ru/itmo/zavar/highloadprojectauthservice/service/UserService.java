package ru.itmo.zavar.highloadprojectauthservice.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.itmo.zavar.highloadprojectauthservice.entity.security.UserEntity;

import java.util.Optional;

public interface UserService {
    UserDetailsService userDetailsService();

    UserEntity saveUser(UserEntity userEntity);

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByUsername(String username);
}
