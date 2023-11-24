package ru.itmo.zavar.highload.zorthtranslator.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

import java.util.Optional;

@Repository
@Transactional
public interface CompilerOutRepository extends JpaRepository<CompilerOutEntity, Long> {
    Optional<CompilerOutEntity> findByRequest(RequestEntity requestEntity);
}
