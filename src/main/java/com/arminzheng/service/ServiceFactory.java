package com.arminzheng.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务实例化类
 *
 * @author zy
 * @since 2022.06.14
 */
public class ServiceFactory {

    public static Properties properties;
    public static Map<Class<?>, Object> map = new ConcurrentHashMap<>();

    static {
        try (InputStream in = ServiceFactory.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                if (name.endsWith("Service")) {
                    Class<?> interfaceClass = Class.forName(name);
                    Class<?> instanceClass = Class.forName(properties.getProperty(name));
                    map.put(interfaceClass, instanceClass.newInstance());
                }
            }
        } catch (IOException
                | ClassNotFoundException
                | InstantiationException
                | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static <T> T getService(Class<T> interfaceClass) {
        return interfaceClass.cast(map.get(interfaceClass));
    }
}
