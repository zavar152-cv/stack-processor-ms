package ru.itmo.zavar.highload.fileservice.util;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WsFileRequestMessage {
    private String clientId;
    private String username;
    private Long fileId;
}
