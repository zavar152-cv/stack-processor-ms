package ru.itmo.zavar.highloadprojectuserservice.dto.outer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.itmo.zavar.highloadprojectuserservice.validator.ValidPassword;

public record AddUserRequest(@NotBlank @Size(min = 5, max = 25) String username, @ValidPassword @NotBlank String password) {
}
