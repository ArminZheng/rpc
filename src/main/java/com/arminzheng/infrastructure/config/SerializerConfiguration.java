package com.arminzheng.infrastructure.config;

import com.arminzheng.infrastructure.utility.SerializerEnum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 使用配置文件 获取 编解码方法
 *
 * @author zy
 */
public abstract class SerializerConfiguration {

    private static final Properties properties;
    private static SerializerEnum.Algorithm algorithm;

    static {
        try (InputStream in =
                SerializerConfiguration.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 从配置文件得到序列化算法
     *
     * @return 序列化算法
     */
    public static SerializerEnum.Algorithm serializerSelector() {
        if (algorithm != null) return algorithm;
        final String value = properties.getProperty("serializer.algorithm");
        if (value == null) algorithm = SerializerEnum.Algorithm.JAVA;
        else algorithm = SerializerEnum.Algorithm.valueOf(value);
        return algorithm;
    }
}
