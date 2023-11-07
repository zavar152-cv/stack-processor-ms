package ru.itmo.zavar.highloadprojectuserservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.response.UserServiceResponse;
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

    @Override
    public UserEntity saveUser(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    public void addUser(String username, String password) throws IllegalArgumentException {
        Optional<RoleEntity> roleUser = roleRepository.findByName(RoleConstants.USER);
        if (roleUser.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }
        UserEntity user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(roleUser.get()))
                .build();
        findByUsername(username).ifPresent(userEntity -> {
            throw new IllegalArgumentException("User exists");
        });
        saveUser(user);
    }

    /*
    public void changeRole(String username, String role) {
        ResponseEntity<UserServiceResponse> response = userServiceClient.getByUsername(username);
        if (response.getStatusCode().isError()) {
            throw new NoSuchElementException();
        }
        UserEntity userEntity = userEntityMapper.fromResponse(response.getBody());
        Optional<RoleEntity> optionalRoleEntity = roleService.findByName(role);
        RoleEntity roleEntity = optionalRoleEntity.orElseThrow();
        userEntity.getRoles().clear();
        userEntity.getRoles().add(roleEntity);
        userServiceClient.save(userEntityMapper.toRequest(userEntity));
    }
     */

    @Override
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    Optional<RoleEntity> findRoleByName(String name) {
        return roleRepository.findByName(name);
    }


}