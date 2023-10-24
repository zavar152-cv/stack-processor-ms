package ru.itmo.zavar.highloadproject.entity.zorth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compiler_out")
public class CompilerOutEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "request_id")
    @NotNull
    private RequestEntity request;

    @ManyToOne //TODO nullable or move it to ProcessorOutEntity
    @JoinColumn(name = "processor_out_id")
    private ProcessorOutEntity processorOut;

    @Lob
    @NotNull
    private byte[] program;

    @Lob
    @NotNull
    private byte[] data;
}
