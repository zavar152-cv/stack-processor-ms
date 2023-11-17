package ru.itmo.zavar.highloadproject.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.ProcessorOutEntity;

import java.util.List;

@Repository
@Transactional
public interface ProcessorOutRepository extends JpaRepository<ProcessorOutEntity, Long> {
    List<ProcessorOutEntity> findAllByCompilerOut(CompilerOutEntity compilerOutEntity);
}
