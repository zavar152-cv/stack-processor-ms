package ru.itmo.zavar.highload.userservice.util;

import java.util.Date;

public record SpringWebErrorModel(Date timestamp, Integer status, String error, String message, String path,
                                  Object errors) {
}
