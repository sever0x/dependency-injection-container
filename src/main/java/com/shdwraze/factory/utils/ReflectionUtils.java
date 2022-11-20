package com.shdwraze.factory.utils;

import com.shdwraze.annotation.PostConstructor;
import com.shdwraze.annotation.Qualifier;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {
    @SneakyThrows
    public static void invokePostConstructorMethod(Object instance) {
        List<Method> methodsWithPostConstructor = getPostConstructorMethods(instance.getClass());

        if (methodsWithPostConstructor.size() == 1) {
            Method method = methodsWithPostConstructor.get(0);
            method.invoke(instance);
        }
    }

    public static List<Method> getPostConstructorMethods(Class<?> classObject) {
        List<Method> methodsWithPostConstructor = new ArrayList<>();

        for (Method method : classObject.getMethods()) {
            if (method.isAnnotationPresent(PostConstructor.class)) {
                methodsWithPostConstructor.add(method);
            }
        }

        return methodsWithPostConstructor;
    }

    public static String getQualifierValue(Parameter parameter) {
        return parameter.getDeclaredAnnotation(Qualifier.class).value();
    }

}
