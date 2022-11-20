package com.shdwraze.factory.utils;

import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

public class BeanUtils {
    @SneakyThrows
    public static Object getBeanInstanceByInterface(Class<?> anInterface, Constructor<?> constructor,
                                                    Map<Object, Class<?>> implementedClasses) {
        for (Map.Entry<Object, Class<?>> entry : implementedClasses.entrySet()) {
            if (entry.getValue().equals(anInterface)) {
                return constructor.newInstance(entry.getKey());
            }
        }

        return null;
    }

    @SneakyThrows
    public static Object getBeanInstanceByName(String beanName, Constructor<?> constructor,
                                               Map<Class<?>, Object> beans) {
        for (Map.Entry<Class<?>, Object> entry : beans.entrySet()) {
            if (entry.getKey().getSimpleName().equals(beanName)) {
                return constructor.newInstance(entry.getValue());
            }
        }
        return null;
    }

    public static void getImplementedInterface(Class<?> classObject, Object instance,
                                               Map<Object, Class<?>> implementedClasses) {
        List<Class<?>> interfaces = List.of(classObject.getInterfaces());

        if (interfaces.size() > 0) {
            for (Class<?> anInterface : interfaces) {
                implementedClasses.put(instance, anInterface);
            }
        }
    }
}
