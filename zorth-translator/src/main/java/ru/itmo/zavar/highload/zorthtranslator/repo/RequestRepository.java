package ru.itmo.zavar.highload.zorthtranslator.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
}
