package ru.itmo.zavar.highloadproject.service;

import ru.itmo.zavar.highloadproject.entity.security.RoleEntity;

import java.util.Optional;

public interface RoleService {

    Optional<RoleEntity> findByName(String name);

    Optional<RoleEntity> getAdminRole();

    Optional<RoleEntity> getVipRole();

    Optional<RoleEntity> getUserRole();

    RoleEntity saveRole(RoleEntity roleEntity);
}
