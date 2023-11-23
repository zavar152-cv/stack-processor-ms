package ru.itmo.zavar.highload.zorthprocessor.entity.zorth;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("processor_out")
public class ProcessorOutEntity {
    @Setter
    @Id
    private Long id;

    private byte[] input;

    private Long compilerOutId;

    private byte[] tickLogs;
}