package com.arminzheng.exercise.buffer.diversity;

import lombok.Cleanup;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * 分散读/集中写
 *
 * <p>new 一个文件通道 -> allocate 分配三个内存 -> read(ByteBuffer[]) 文件中分散读取到三个内存 -> flip 三个内存调试打印
 */
public class TestScatteringReads {

    /**
     * 分散读
     *
     * <p>channel.read(ByteBuffer[])
     */
    public static void main(String[] args) throws IOException {
        ByteBuffer bu1 = ByteBuffer.allocate(3);
        ByteBuffer bu2 = ByteBuffer.allocate(3);
        ByteBuffer bu3 = ByteBuffer.allocate(5);

        @Cleanup RandomAccessFile file = new RandomAccessFile("words.txt", "rw");
        FileChannel channel = file.getChannel();
        // 顺序分散读取到三个ByteBuffer中
        channel.read(new ByteBuffer[] {bu1, bu2, bu3});

        bu1.flip();
        bu2.flip();
        bu3.flip();
        debugAll(bu1);
        debugAll(bu2);
        debugAll(bu3);
    }
}
