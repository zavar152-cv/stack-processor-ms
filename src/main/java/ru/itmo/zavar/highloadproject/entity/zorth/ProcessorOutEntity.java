package ru.itmo.zavar.highloadproject.entity.zorth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processor_out")
public class ProcessorOutEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @NotBlank
    private String input;

    @ManyToOne
    @JoinColumn(name = "compiler_out_id")
    private CompilerOutEntity compilerOut;

    @Lob
    @NotNull
    @Column(name = "tick_logs")
    private String tickLogs;
}
