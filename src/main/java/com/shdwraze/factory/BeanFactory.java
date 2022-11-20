package com.shdwraze.factory;

import com.shdwraze.annotation.Autowired;
import com.shdwraze.annotation.Qualifier;
import com.shdwraze.stereotype.Component;
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

    private BeanFactory() {
        beans = new HashMap<>();
        implementedClasses = new HashMap<>();
        componentClasses = new HashMap<>();
    }

    public static void run(Class<?> initClass) {
        BeanFactory.BeanFactoryInitialization.BEAN_FACTORY.instantiate(initClass.getPackageName());
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
                        }
                    }
                }
                if (!implementedClasses.containsKey(beans.get(classEntry))) {
                    getImplementedInterface(classEntry, beans.get(classEntry));
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
                    return getBeanInstanceByName(getQualifierValue(parameter), constructor);
                }
                return getBeanInstanceByInterface(parameter.getType(), constructor);
            }
        }

        return null;
    }

    @SneakyThrows
    private Object getBeanInstanceByInterface(Class<?> anInterface, Constructor<?> constructor) {
        for (Map.Entry<Object, Class<?>> entry : implementedClasses.entrySet()) {
            if (entry.getValue().equals(anInterface)) {
                return constructor.newInstance(entry.getKey());
            }
        }

        return null;
    }

    @SneakyThrows
    private Object getBeanInstanceByName(String beanName, Constructor<?> constructor) {
        for (Map.Entry<Class<?>, Object> entry : beans.entrySet()) {
            if (entry.getKey().getSimpleName().equals(beanName)) {
                return constructor.newInstance(entry.getValue());
            }
        }
        return null;
    }

    private void getImplementedInterface(Class<?> classObject, Object instance) {
        List<Class<?>> interfaces = List.of(classObject.getInterfaces());

        if (interfaces.size() > 0) {
            for (Class<?> anInterface : interfaces) {
                implementedClasses.put(instance, anInterface);
            }
        }
    }

    private String getQualifierValue(Parameter parameter) {
        return parameter.getDeclaredAnnotation(Qualifier.class).value();
    }

    private static class BeanFactoryInitialization {
        private static final BeanFactory BEAN_FACTORY = new BeanFactory();
    }
}
