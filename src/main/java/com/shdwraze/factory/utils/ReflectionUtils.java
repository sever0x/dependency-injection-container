package com.shdwraze.factory.utils;

import com.shdwraze.annotation.PostConstructor;
import com.shdwraze.annotation.Qualifier;
import com.shdwraze.exception.AnnotationException;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {
    @SneakyThrows
    public static void invokePostConstructorMethod(Object instance) {
        List<Method> methodsWithPostConstructor = getPostConstructorMethods(instance.getClass());

        if (methodsWithPostConstructor.size() > 0) {
            for (Method method : methodsWithPostConstructor) {
                method.invoke(instance);
            }
        }
    }

    @SneakyThrows
    public static List<Method> getPostConstructorMethods(Class<?> classObject) {
        List<Method> methodsWithPostConstructor = new ArrayList<>();

        for (Method method : classObject.getMethods()) {
            if (method.isAnnotationPresent(PostConstructor.class)) {
                if (method.getParameters().length == 0)
                    methodsWithPostConstructor.add(method);
                else throw new AnnotationException(PostConstructor.class.getSimpleName()
                        + " can only be applied to void methods");
            }
        }

        return methodsWithPostConstructor;
    }

    public static String getQualifierValue(Parameter parameter) {
        return parameter.getDeclaredAnnotation(Qualifier.class).value();
    }

}
