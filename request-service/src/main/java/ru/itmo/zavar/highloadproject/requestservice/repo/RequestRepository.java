package ru.itmo.zavar.highloadproject.requestservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highloadproject.requestservice.entity.zorth.RequestEntity;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
}
