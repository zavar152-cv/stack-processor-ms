package ru.itmo.zavar.highloadproject.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;

import java.util.Optional;

@Repository
@Transactional
public interface CompilerOutRepository extends JpaRepository<CompilerOutEntity, Long> {
    Optional<CompilerOutEntity> findById(Long id);

    Optional<CompilerOutEntity> findByRequest(RequestEntity requestEntity);

    void deleteByRequest(RequestEntity requestEntity);
}
