package ru.itmo.zavar.highloadprojectuserservice.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.RoleEntity;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.UserEntity;
import ru.itmo.zavar.highloadprojectuserservice.repo.RoleRepository;
import ru.itmo.zavar.highloadprojectuserservice.repo.UserRepository;
import ru.itmo.zavar.highloadprojectuserservice.service.UserService;
import ru.itmo.zavar.highloadprojectuserservice.util.RoleConstants;

import java.util.NoSuchElementException;
import java.util.Optional;
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
        RoleEntity adminRole = findRoleByName(RoleConstants.ADMIN).orElseGet(() -> saveRole(new RoleEntity(0L, RoleConstants.ADMIN)));
        findRoleByName(RoleConstants.VIP).orElseGet(() -> saveRole(new RoleEntity(1L, RoleConstants.VIP)));
        findRoleByName(RoleConstants.USER).orElseGet(() -> saveRole(new RoleEntity(2L, RoleConstants.USER)));
        findByUsername(adminUsername).orElseGet(() -> {
            UserEntity admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole)).build();
            return saveUser(admin);
        });
    }

    @Override
    public UserEntity saveUser(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    public RoleEntity saveRole(RoleEntity roleEntity) {
        return roleRepository.save(roleEntity);
    }

    @Override
    public void addUser(String username, String password) throws IllegalArgumentException {
        Optional<RoleEntity> roleUser = roleRepository.findByName(RoleConstants.USER);
        if (roleUser.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }
        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(roleUser.get()))
                .build();
        findByUsername(username).ifPresent(entity -> {
            throw new IllegalArgumentException("User exists");
        });
        saveUser(userEntity);
    }

    @Override
    public void changeRole(String username, String role) throws NoSuchElementException {
        Optional<UserEntity> optionalUserEntity = findByUsername(username);
        UserEntity userEntity = optionalUserEntity.orElseThrow();
        Optional<RoleEntity> optionalRoleEntity = findRoleByName(role);
        RoleEntity roleEntity = optionalRoleEntity.orElseThrow();
        userEntity.getRoles().clear();
        userEntity.getRoles().add(roleEntity);
        saveUser(userEntity);
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<RoleEntity> findRoleByName(String name) {
        return roleRepository.findByName(name);
    }

}