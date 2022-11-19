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

    private Map<String, Object> beans;

    private Set<Class<?>> componentClasses;

    private BeanFactory() {
        beans = new HashMap<>();
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

            for (Constructor<?> constructor : constructors) {
                if (!constructor.isAnnotationPresent(Autowired.class)) {
                    createInstanceWithNoArgsConstructor(componentClass);
                } else {
                    System.out.println("@Autowired: " + constructor.getName());
                    createAutowiredInstance(componentClass, constructor);
                }
            }
        }
    }

    @SneakyThrows
    private void createInstanceWithNoArgsConstructor(Class<?> classObject) {
        Object instance = classObject.getConstructor().newInstance();

        String className = classObject.getSimpleName();
        String bean = className.substring(0, 1).toLowerCase() + className.substring(1);
        beans.put(bean, instance);
    }

    @SneakyThrows
    private void createAutowiredInstance(Class<?> classObject, Constructor<?> constructor) {
        List<Parameter> parameters = List.of(constructor.getParameters());
        for (Parameter parameter : parameters) {
            for (Object dependency : beans.values()) {
                if (dependency.getClass().equals(parameter.getType())) {
                    String objectName = classObject.getSimpleName();

                    Object instance = constructor.newInstance(dependency);

                    String bean = objectName.substring(0, 1).toLowerCase()
                            + objectName.substring(1);
                    beans.put(bean, instance);
                }
            }
        }
    }

    private static class BeanFactoryInitialization {
        private static final BeanFactory BEAN_FACTORY = new BeanFactory();
    }
}
