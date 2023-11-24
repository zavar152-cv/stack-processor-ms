package ru.itmo.zavar.highload.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.GetCompilerOutResponse;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highload.zorthtranslator.util.ZorthUtil;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface CompilerOutEntityMapper {
    @Mapping(source = "request", target = "requestId")
    GetCompilerOutResponse toDTO(CompilerOutEntity entity);

    default Long map(RequestEntity request) {
        return request == null ? null : request.getId();
    }

    default ArrayList<Long> map(byte[] data) {
        return data == null ? null : ZorthUtil.fromByteArrayToLongList(data);
    }
}
