package ru.itmo.zavar.highloadproject.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
}
