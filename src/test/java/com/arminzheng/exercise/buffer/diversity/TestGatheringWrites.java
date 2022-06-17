package com.arminzheng.exercise.buffer.diversity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 分散读/集中写
 *
 * <p>获取三个 buffer -> getChannel 获取通道 -> write(ByteBuffer[]) 集中写
 *
 * @author zy
 */
public class TestGatheringWrites {

    public static void main(String[] args) {
        ByteBuffer h = UTF_8.encode("Hello");
        ByteBuffer w = UTF_8.encode("word");
        ByteBuffer nh = UTF_8.encode("你好");
        // debugAll(b2);
        try (RandomAccessFile rw = new RandomAccessFile("data.txt", "rw")) {
            FileChannel ch = rw.getChannel();
            ch.write(new ByteBuffer[] {h, w, nh});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() {
        ByteBuffer hello = ByteBuffer.allocate(5);
        ByteBuffer chinese = ByteBuffer.allocate(6);
        hello.put(new byte[] {'h', 'e', 'l', 'l', 'o'}); // 数组长度不能超过 bytebuffer
        chinese.put("中文".getBytes());

        hello.flip();
        debugAll(hello);
        chinese.flip();
        debugAll(chinese);
        System.out.println("一个汉字占用三个字节 = " + UTF_8.decode(chinese));
    }
}
