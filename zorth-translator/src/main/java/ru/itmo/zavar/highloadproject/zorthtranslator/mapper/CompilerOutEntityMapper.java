package ru.itmo.zavar.highloadproject.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetAllCompilerOutResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response.GetCompilerOutResponse;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.util.ZorthUtil;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface CompilerOutEntityMapper {
    GetCompilerOutResponse toDTO(CompilerOutEntity entity);

    @Mapping(source = "request", target = "requestId")
    GetAllCompilerOutResponse toDetailedDTO(CompilerOutEntity entity);

    default Long map(RequestEntity request) {
        return request == null ? null : request.getId();
    }

    default ArrayList<Long> map(byte[] data) {
        return data == null ? null : ZorthUtil.fromByteArrayToLongList(data);
    }
}
