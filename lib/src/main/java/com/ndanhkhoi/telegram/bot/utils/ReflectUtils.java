package com.ndanhkhoi.telegram.bot.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created at 08:59:19 August 31, 2022,
 */
@UtilityClass
public class ReflectUtils {

    public static List<Field> getAllFieldsList(final Class<?> clazz) {
        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

    public static Field getDeclaredField(Class<?> clazz, String name) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return clazz.getDeclaredField(name);
            }
            catch (NoSuchFieldException ex) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    @SneakyThrows
    public static <T> Object getProperty(T bean, String name) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), name);
        if (pd != null && pd.getReadMethod() != null) {
            return pd.getReadMethod().invoke(bean);
        }
        return null;
    }

    @SneakyThrows
    public static <T> Object setProperty(T bean, String name, Object value) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(bean.getClass(), name);
        if (pd != null && pd.getWriteMethod() != null) {
            return pd.getWriteMethod().invoke(bean, value);
        }
        return null;
    }

}
