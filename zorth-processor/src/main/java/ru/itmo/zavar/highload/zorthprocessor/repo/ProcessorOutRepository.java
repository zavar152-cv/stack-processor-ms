package ru.itmo.zavar.highload.zorthprocessor.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthprocessor.entity.zorth.ProcessorOutEntity;

public interface ProcessorOutRepository extends ReactiveCrudRepository<ProcessorOutEntity, Long> {
    @Query("INSERT INTO processor_out (compiler_out_id, input, tick_logs) " +
            "VALUES (:compilerOutId, lo_from_bytea(0, :input), lo_from_bytea(0, :tickLogs))" +
            "RETURNING id")
    Mono<Long> saveWithLargeObjects(Long compilerOutId, byte[] input, byte[] tickLogs);

    @Query("SELECT id, compiler_out_id, lo_get(input) AS input, lo_get(tick_logs) AS tick_logs FROM processor_out " +
            "WHERE compiler_out_id = :compilerOutId")
    Flux<ProcessorOutEntity> findAllByCompilerOutId(Long compilerOutId);
}
