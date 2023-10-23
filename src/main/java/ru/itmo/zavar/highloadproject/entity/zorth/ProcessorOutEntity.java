package ru.itmo.zavar.highloadproject.entity.zorth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.zavar.log.TickLog;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processor_out")
public class ProcessorOutEntity {
    @Id
    private Long id;

    @NotBlank
    private String input;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "processor_out_tick_logs", joinColumns = @JoinColumn(name = "compiler_out_id"))
    @NotNull
    private List<String> tickLogs;
}
