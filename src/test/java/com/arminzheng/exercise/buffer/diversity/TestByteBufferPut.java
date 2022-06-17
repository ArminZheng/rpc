package com.arminzheng.exercise.buffer.diversity;

import java.nio.ByteBuffer;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * java nio 的 ByteBuffer 的使用：存放
 *
 * <p>1. 分配内存后
 *
 * <p>2. put 几个字节
 *
 * <p>3. flip 到读模式
 *
 * <p>查看内存情况
 *
 * <p>4. get 一个字节
 *
 * <p>查看内存情况
 *
 * <p>5. compact 一下
 *
 * <p>查看内存情况
 *
 * <p>6. put 几个字节
 *
 * <p>查看内存情况
 */
public class TestByteBufferPut {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x61); //  字符 'a' ; 16进制的97
        debugAll(buffer);
        buffer.put(new byte[] {0x62, 0x63, 0x64}); //  字符 'b','c','d'
        debugAll(buffer);
        // 切换读模式
        buffer.flip(); // 3. 切换至读模式: position指针指向开头， limit指向写入的最后位置 (内存长度)
        // 读取一个字节
        byte byte1 = buffer.get();
        System.out.println("读取一个字节 十进制=" + byte1 + "字符为=" + (char) byte1); // 'a' 十进制的 97 十六进制的 0x61

        // 未读完的部分向前压缩，然后切换至写模式，下标position到压缩字符长度位置，limit到写入限制
        debugAll(buffer);
        buffer.compact();
        debugAll(buffer);
        buffer.put(new byte[] {0x65, 0x6f}); //  字符 'b','c','d' 沿着下标position位置往后存数据
        debugAll(buffer);
    }
}
