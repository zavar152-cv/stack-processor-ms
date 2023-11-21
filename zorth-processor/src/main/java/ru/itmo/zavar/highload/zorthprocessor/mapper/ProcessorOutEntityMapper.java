package ru.itmo.zavar.highload.zorthprocessor.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.response.ProcessorOutDTO;
import ru.itmo.zavar.highload.zorthprocessor.entity.zorth.ProcessorOutEntity;

@Mapper(componentModel = "spring")
public interface ProcessorOutEntityMapper {
    ProcessorOutDTO toDTO(ProcessorOutEntity entity);

    default String mapByteArrayToString(byte[] array) {
        return array == null ? null : new String(array);
    }

    default String[] mapByteArrayToStringArray(byte[] array) {
        return mapByteArrayToString(array).split("\n");
    }
}
