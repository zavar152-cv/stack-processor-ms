package ru.itmo.zavar.highloadprojectrequestservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.zavar.highloadprojectrequestservice.entity.zorth.RequestEntity;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
}
