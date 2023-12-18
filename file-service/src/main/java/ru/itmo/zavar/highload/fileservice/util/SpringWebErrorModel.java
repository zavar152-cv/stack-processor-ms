package ru.itmo.zavar.highload.fileservice.util;

import java.util.Date;

public record SpringWebErrorModel(Date timestamp, Integer status, String error, String message, String path,
                                  Object errors) {
}
