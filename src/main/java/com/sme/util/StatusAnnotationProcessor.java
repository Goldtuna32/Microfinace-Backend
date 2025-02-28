package com.sme.util;

import com.sme.annotation.StatusConverter;
import com.sme.entity.Status;


import java.lang.reflect.Field;

public class StatusAnnotationProcessor {

    public static void processStatus(Object dto) {
        Class<?> clazz = dto.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(StatusConverter.class) && field.getType() == int.class) {
                field.setAccessible(true);
                try {
                    int statusCode = field.getInt(dto);
                    Status status = Status.fromCode(statusCode);
                    System.out.println("Converted " + statusCode + " to " + status);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
