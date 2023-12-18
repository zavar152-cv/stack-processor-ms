package ru.itmo.zavar.highload.zorthtranslator.util;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WsFileResponseMessage {
    private Boolean exists;
    private Boolean owned;
    private String fileName;
    private String content;
}
