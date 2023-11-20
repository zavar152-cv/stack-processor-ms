package ru.itmo.zavar.highload.userservice.dto.outer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.itmo.zavar.highload.userservice.validator.ValidPassword;

public record AddUserRequest(@NotBlank @Size(min = 5, max = 25) String username, @ValidPassword String password) {
}
