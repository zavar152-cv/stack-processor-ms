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
    private RequestEntity request;

    @ManyToOne
    @JoinColumn(name = "processor_out_id")
    private ProcessorOutEntity processorOut;

    @ElementCollection(targetClass = Byte.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "compiler_out_program", joinColumns = @JoinColumn(name = "compiler_out_id"))
    @NotNull
    private List<Byte[]> program;

    @ElementCollection(targetClass = Byte.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "compiler_out_data", joinColumns = @JoinColumn(name = "compiler_out_id"))
    @NotNull
    private List<Byte[]> data;
}
