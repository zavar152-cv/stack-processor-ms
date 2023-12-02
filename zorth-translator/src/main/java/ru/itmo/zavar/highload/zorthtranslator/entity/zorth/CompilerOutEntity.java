package ru.itmo.zavar.highload.zorthtranslator.entity.zorth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private RequestEntity request;

    @Lob
    @NotNull
    private byte[] program;

    @Lob
    @NotNull
    private byte[] data;
}
