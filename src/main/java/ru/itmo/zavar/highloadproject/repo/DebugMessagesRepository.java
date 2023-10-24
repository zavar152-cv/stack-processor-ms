package ru.itmo.zavar.highloadproject.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;

import java.util.Optional;

@Repository
public interface DebugMessagesRepository extends JpaRepository<DebugMessagesEntity, Long> {
    Optional<DebugMessagesEntity> findById(Long id);

    void deleteByRequest(RequestEntity requestEntity);
}
