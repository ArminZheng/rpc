package com.arminzheng.infrastructure.rpc.handler.codec;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * 对 gson 不支持Class类转换导致的异常做处理
 *
 * @author zy
 */
public class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
    @Override
    public Class<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            String str = json.getAsString();
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(Class<?> src, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName()); // 基本数据类型
    }
}
