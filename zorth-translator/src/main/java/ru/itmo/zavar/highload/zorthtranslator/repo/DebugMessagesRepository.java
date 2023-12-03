package ru.itmo.zavar.highload.zorthtranslator.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

import java.util.Optional;

@Repository
@Transactional
public interface DebugMessagesRepository extends JpaRepository<DebugMessagesEntity, Long> {
    Optional<DebugMessagesEntity> findByRequest(RequestEntity requestEntity);
}
