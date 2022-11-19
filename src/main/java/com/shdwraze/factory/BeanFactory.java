package com.shdwraze.factory;

import com.shdwraze.annotation.Autowired;
import com.shdwraze.stereotype.Component;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class BeanFactory {

    private Map<Class<?>, Object> beans;

    private Map<Object, Class<?>> implementedClasses;

    private Set<Class<?>> componentClasses;

    private BeanFactory() {
        beans = new HashMap<>();
        implementedClasses = new HashMap<>();
        componentClasses = new HashSet<>();
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
        System.out.println(beans);
    }

    @SneakyThrows
    private void scanComponent(File file, String basePackage) {
        for (File classFile : Objects.requireNonNull(file.listFiles())) {
            String fileName = classFile.getName();

            if (fileName.endsWith(".class")) {
                String className = fileName.substring(0, fileName.lastIndexOf('.'));
                Class<?> classObject = Class.forName(basePackage + "." + className);

                if (classObject.isAnnotationPresent(Component.class)) {
                    System.out.println("@Component: " + classObject);

                    componentClasses.add(classObject);
                }
            } else {
                scanComponent(classFile, basePackage + "." + fileName);
            }
        }
    }

    @SneakyThrows
    private void createInstance() {
        for (Class<?> componentClass : componentClasses) {
            List<Constructor<?>> constructors = List.of(componentClass.getDeclaredConstructors());

            Object instance;
            for (Constructor<?> constructor : constructors) {
                if (!constructor.isAnnotationPresent(Autowired.class)) {
                    instance = createInstanceWithNoArgsConstructor(componentClass);
                } else {
                    System.out.println("@Autowired: " + constructor.getName());
                    instance = createAutowiredInstance(constructor);
                }

                beans.put(componentClass, instance);
            }
            getImplementedInterface(componentClass, beans.get(componentClass));
        }
    }

    private void getImplementedInterface(Class<?> classObject, Object instance) {
        List<Class<?>> interfaces = List.of(classObject.getInterfaces());

        if (interfaces.size() > 0) {
            for (Class<?> anInterface : interfaces) {
                implementedClasses.put(instance, anInterface);
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
                return getBeanByInterface(parameter.getType(), constructor);
            }
        }

        return null;
    }

    @SneakyThrows
    private Object getBeanByInterface(Class<?> anInterface, Constructor<?> constructor) {
        for (Map.Entry<Object, Class<?>> entry : implementedClasses.entrySet()) {
            if (entry.getValue().equals(anInterface)) {
                return constructor.newInstance(entry.getKey());
            }
        }

        return null;
    }

    private static class BeanFactoryInitialization {
        private static final BeanFactory BEAN_FACTORY = new BeanFactory();
    }
}
