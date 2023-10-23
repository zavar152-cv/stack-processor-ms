package ru.itmo.zavar.highloadproject.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;

@Repository
public interface CompilerOutRepository extends JpaRepository<CompilerOutEntity, Long> {
}
