package ru.itmo.zavar.highloadproject.userservice.dto.outer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.itmo.zavar.highloadproject.userservice.validator.ValidPassword;

public record AddUserRequestDTO(@NotBlank @Size(min = 5, max = 25) String username, @ValidPassword String password) {
}
