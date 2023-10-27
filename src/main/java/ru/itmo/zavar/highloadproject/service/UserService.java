package ru.itmo.zavar.highloadproject.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserService {
    UserDetailsService userDetailsService();

    UserEntity saveUser(UserEntity userEntity);

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByUsername(String username);
}
