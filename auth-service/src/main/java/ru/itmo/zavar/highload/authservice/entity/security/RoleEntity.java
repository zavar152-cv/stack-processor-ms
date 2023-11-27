package ru.itmo.zavar.highload.authservice.entity.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {
    private Long id;

    private String name;
}
