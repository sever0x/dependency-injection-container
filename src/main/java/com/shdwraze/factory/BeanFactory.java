package com.shdwraze.factory;

import com.shdwraze.annotation.Autowired;
import com.shdwraze.annotation.Qualifier;
import com.shdwraze.annotation.stereotype.Component;
import com.shdwraze.factory.utils.BeanUtils;
import com.shdwraze.factory.utils.ReflectionUtils;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class BeanFactory {

    private final Map<Class<?>, Object> beans;

    private final Map<Object, Class<?>> implementedClasses;

    private final Map<Class<?>, Boolean> componentClasses;

    protected BeanFactory() {
        beans = new HashMap<>();
        implementedClasses = new HashMap<>();
        componentClasses = new HashMap<>();
    }

    public static void run(Class<?> initClass) {
        BeanFactoryInitialization.BEAN_FACTORY.instantiate(initClass.getPackageName());
    }

    @SneakyThrows
    private void instantiate(String basePackage) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        String path = basePackage.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.toURI());
            scanComponent(file, basePackage);
        }

        createInstance();
    }

    @SneakyThrows
    private void scanComponent(File file, String basePackage) {
        for (File classFile : Objects.requireNonNull(file.listFiles())) {
            String fileName = classFile.getName();

            if (fileName.endsWith(".class")) {
                String className = fileName.substring(0, fileName.lastIndexOf('.'));
                Class<?> classObject = Class.forName(basePackage + "." + className);

                if (classObject.isAnnotationPresent(Component.class)) {
                    componentClasses.put(classObject, false);
                }
            } else {
                scanComponent(classFile, basePackage + "." + fileName);
            }
        }
    }

    private void createInstance() {
        while (componentClasses.containsValue(Boolean.FALSE)) {
            for (Map.Entry<Class<?>, Boolean> entry : componentClasses.entrySet()) {
                Class<?> classEntry = entry.getKey();

                if (entry.getValue().equals(Boolean.FALSE)) {
                    for (Constructor<?> constructor : entry.getKey().getDeclaredConstructors()) {
                        Object instance = !constructor.isAnnotationPresent(Autowired.class)
                                ? createInstanceWithNoArgsConstructor(classEntry)
                                : createAutowiredInstance(constructor);

                        if (instance != null) {
                            entry.setValue(Boolean.TRUE);
                            beans.put(classEntry, instance);
                            ReflectionUtils.invokePostConstructorMethod(instance);
                        }
                    }
                }
                if (!implementedClasses.containsKey(beans.get(classEntry))) {
                    BeanUtils.getImplementedInterface(classEntry, beans.get(classEntry), implementedClasses);
                }
            }
        }
    }

    @SneakyThrows
    private Object createInstanceWithNoArgsConstructor(Class<?> classObject) {
        return classObject.getConstructor().newInstance();
    }

    @SneakyThrows
    private Object createAutowiredInstance(Constructor<?> constructor) {
        List<Parameter> parameters = List.of(constructor.getParameters());
        for (Parameter parameter : parameters) {
            for (Object dependency : beans.values()) {
                if (dependency.getClass().equals(parameter.getType())) {
                    return constructor.newInstance(dependency);
                }
            }
            if (parameter.getType().isInterface()) {
                if (parameter.isAnnotationPresent(Qualifier.class)) {
                    return BeanUtils.getBeanInstanceByName(ReflectionUtils.getQualifierValue(parameter), constructor, beans);
                }
                return BeanUtils.getBeanInstanceByInterface(parameter.getType(), constructor, implementedClasses);
            }
        }

        return null;
    }

    private static class BeanFactoryInitialization {
        private static final BeanFactory BEAN_FACTORY = new BeanFactory();
    }
}
