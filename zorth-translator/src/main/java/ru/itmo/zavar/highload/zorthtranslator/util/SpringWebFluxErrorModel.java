package ru.itmo.zavar.highload.zorthtranslator.util;

import java.util.Date;

public record SpringWebFluxErrorModel(Date timestamp, String path, Integer status, String error, String message,
                                      String requestId, Object errors) {
}
