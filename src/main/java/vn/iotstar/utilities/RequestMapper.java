package vn.iotstar.utilities;

import java.lang.reflect.Field;
import jakarta.servlet.http.HttpServletRequest;

public class RequestMapper {

    public static <T> T mapRequestToEntity(HttpServletRequest request, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String value = request.getParameter(field.getName());

                if (value != null && !value.isEmpty()) {
                    Class<?> type = field.getType();

                    if (type == String.class) {
                        field.set(instance, value);
                    } else if (type == int.class || type == Integer.class) {
                        field.set(instance, Integer.parseInt(value));
                    } else if (type == long.class || type == Long.class) {
                        field.set(instance, Long.parseLong(value));
                    } else if (type == boolean.class || type == Boolean.class) {
                        field.set(instance, Boolean.parseBoolean(value));
                    } else if (type == double.class || type == Double.class) {
                        field.set(instance, Double.parseDouble(value));
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping request to entity: " + clazz.getSimpleName(), e);
        }
    }
}
