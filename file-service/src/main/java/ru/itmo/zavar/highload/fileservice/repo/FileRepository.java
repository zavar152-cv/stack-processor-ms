package ru.itmo.zavar.highload.fileservice.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highload.fileservice.entity.FileEntity;

import java.util.Optional;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, Long> {
    Optional<FileEntity> findByName(String name);
    void deleteByName(String name);
}
