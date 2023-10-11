package ru.itmo.zavar.highloadproject.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highloadproject.entity.TestEntity;

@Repository
public interface TestEntityRepository extends CrudRepository<TestEntity, Long> {
    @Override
    Iterable<TestEntity> findAll();
}
