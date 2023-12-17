package ru.itmo.zavar.highload.userservice.dto.outer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.itmo.zavar.highload.userservice.validator.ValidPassword;

public record AddUserRequest(@Schema(example = "userr") @NotBlank @Size(min = 5, max = 25) String username,
                             @Schema(example = "Us3r----", requiredMode = Schema.RequiredMode.REQUIRED) @ValidPassword String password) {
}
