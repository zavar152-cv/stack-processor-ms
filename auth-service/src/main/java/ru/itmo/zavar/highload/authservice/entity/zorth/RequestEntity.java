package ru.itmo.zavar.highload.authservice.entity.zorth;

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
public class RequestEntity {
    private Long id;

    @NotBlank
    private String text;

    @NotNull
    private Boolean debug;
}
