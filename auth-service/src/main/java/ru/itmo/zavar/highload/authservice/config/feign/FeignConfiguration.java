package ru.itmo.zavar.highload.authservice.config.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import ru.itmo.zavar.highload.authservice.error.CustomErrorDecoder;

public class FeignConfiguration {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder(new ObjectMapper());
    }
}
