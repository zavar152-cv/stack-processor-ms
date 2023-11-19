package ru.itmo.zavar.highloadproject.zorthtranslator.util;

import org.apache.commons.lang3.ArrayUtils;
import ru.itmo.zavar.InstructionCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ZorthUtil {
    public static <T> List<T[]> splitArray(final T[] array) {

        int numberOfArrays = array.length / Long.BYTES;
        int remainder = array.length % Long.BYTES;

        int start = 0;
        int end = 0;

        List<T[]> list = new ArrayList<T[]>();
        for (int i = 0; i < numberOfArrays; i++) {
            end += Long.BYTES;
            list.add(Arrays.copyOfRange(array, start, end));
            start = end;
        }

        if (remainder > 0) {
            list.add(Arrays.copyOfRange(array, start, (start + remainder)));
        }
        return list;
    }

    public static ArrayList<Long> fromByteArrayToLongList(byte[] bytes) {
        ArrayList<Long> list = new ArrayList<>();
        List<Byte[]> temp = splitArray(ArrayUtils.toObject(bytes));
        temp.forEach(t -> list.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(t))));
        return list;
    }
}
