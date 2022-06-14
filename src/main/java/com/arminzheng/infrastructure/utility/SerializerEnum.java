package com.arminzheng.infrastructure.utility;

import com.arminzheng.infrastructure.rpc.handler.codec.ClassCodec;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 序列化选择器
 *
 * @author zy
 */
public interface SerializerEnum {

    <T> T deserialize(Class<T> clazz, byte[] bytes);

    <T> byte[] serialize(T object);

    // default, public static final
    Gson GSON = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();

    enum Algorithm implements SerializerEnum {
        JAVA {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    final ObjectInputStream ois = new ObjectInputStream(bis);
                    return clazz.cast(ois.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("反序列化算法失败！");
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(); // 字节数组
                    ObjectOutputStream oos = new ObjectOutputStream(bos); // 对象流包装
                    oos.writeObject(object);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("序列化算法失败！", e);
                }
            }
        },
        JSON {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                final String json = new String(bytes, StandardCharsets.UTF_8);
                return GSON.fromJson(json, clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                final String json = GSON.toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }
    }
}
