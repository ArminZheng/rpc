package com.arminzheng.exercise.buffer.diversity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * 字符串 输入 到 ByteBuffer 里面去
 *
 * @since 2022-05-29
 */
public class TestEncodeToByteBuffer {

    public static void main(String[] args) {
        ByteBuffer bu1 = ByteBuffer.allocate(16);
        // 1. 字符串转字节数组 ===> put进去
        bu1.put("hello".getBytes());
        debugAll(bu1);
        bu1.flip();
        // 内存里的 ByteBuffer 转为 字符串
        System.out.println("ByteBuffer 转为 字符串: " + StandardCharsets.UTF_8.decode(bu1));

        // 2 使用字符集类 Charset, 它本身就支持和 ByteBuffer 相互转换, 自动切换为读模式
        ByteBuffer bu2 = StandardCharsets.UTF_8.encode("world");
        debugAll(bu2);
        System.out.println("字符集类和 ByteBuffer 相互转换: " + StandardCharsets.UTF_8.decode(bu2));

        // 3. nio提供的工具类 wrap 方法，将字节数组 ===> 包装成ByteBuffer, 自动切换为读模式
        ByteBuffer bu3 = ByteBuffer.wrap("Hello world".getBytes());
        debugAll(bu3);
        System.out.println("nio提供的工具类 wrap 方法: " + StandardCharsets.UTF_8.decode(bu3));
    }
}
