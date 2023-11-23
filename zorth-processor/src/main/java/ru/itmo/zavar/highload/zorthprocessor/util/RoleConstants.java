package ru.itmo.zavar.highload.zorthprocessor.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleConstants {
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String VIP = "ROLE_VIP";
    public static final String USER = "ROLE_USER";
}