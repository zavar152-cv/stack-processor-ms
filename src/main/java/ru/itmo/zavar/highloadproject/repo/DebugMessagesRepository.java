package ru.itmo.zavar.highloadproject.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;

@Repository
public interface DebugMessagesRepository extends JpaRepository<DebugMessagesEntity, Long> {
}
