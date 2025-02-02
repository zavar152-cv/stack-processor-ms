package ru.itmo.zavar.highload.userservice.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highload.userservice.service.UserService;
import ru.itmo.zavar.highload.userservice.util.RoleConstants;
import ru.itmo.zavar.highload.userservice.entity.security.RoleEntity;
import ru.itmo.zavar.highload.userservice.entity.security.UserEntity;
import ru.itmo.zavar.highload.userservice.repo.RoleRepository;
import ru.itmo.zavar.highload.userservice.repo.UserRepository;

import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    @Transactional
    public void init() {
        RoleEntity adminRole = roleRepository.findByName(RoleConstants.ADMIN)
                .orElseGet(() -> saveRole(new RoleEntity(1L, RoleConstants.ADMIN)));
        roleRepository.findByName(RoleConstants.VIP).orElseGet(() -> saveRole(new RoleEntity(2L, RoleConstants.VIP)));
        roleRepository.findByName(RoleConstants.USER).orElseGet(() -> saveRole(new RoleEntity(3L, RoleConstants.USER)));
        userRepository.findByUsername(adminUsername).orElseGet(() -> {
            UserEntity admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole)).build();
            return saveUser(admin);
        });
    }

    @Override
    public void addUser(String username, String password) throws NoSuchElementException, IllegalArgumentException, DataAccessException {
        RoleEntity roleUser = findRoleByName(RoleConstants.USER);
        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(roleUser))
                .build();
        userRepository.findByUsername(username).ifPresent(entity -> {
            throw new IllegalArgumentException("User exists");
        });
        saveUser(userEntity);
    }

    @Override
    public void changeRole(String username, String role) throws NoSuchElementException, DataAccessException {
        UserEntity userEntity = findUserByUsername(username);
        RoleEntity roleEntity = findRoleByName(role);
        userEntity.getRoles().clear();
        userEntity.getRoles().add(roleEntity);
        saveUser(userEntity);
    }

    @Override
    public UserEntity saveUser(UserEntity userEntity) throws DataAccessException {
        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity findUserByUsername(String username) throws NoSuchElementException {
        return userRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Override
    public RoleEntity saveRole(RoleEntity roleEntity) throws DataAccessException {
        return roleRepository.save(roleEntity);
    }

    @Override
    public RoleEntity findRoleByName(String name) throws NoSuchElementException {
        return roleRepository.findByName(name).orElseThrow(() -> new NoSuchElementException("Role not found"));
    }
}