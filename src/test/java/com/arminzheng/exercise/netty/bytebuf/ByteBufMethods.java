package com.arminzheng.exercise.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * ByteBuf 方法
 *
 * <p>ByteBuf.markReaderIndex 做一个标记
 *
 * <p>ByteBuf.resetReaderIndex 重置到上面标记【可重复读】
 */
public class ByteBufMethods {

    public static void main(String[] args) {
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeBytes(new byte[] {1, 2, 3, 4}); // 写入字节数组
        log(buf);
        buf.writeInt(5); // 先写高位【占用 4个字节】
        log(buf);
        buf.writeIntLE(6); // 先写低位【占用 4个字节】
        log(buf);

        // 读取数据
        System.out.println(buf.readByte()); // 1
        System.out.println(buf.readByte()); // 2
        System.out.println(buf.readByte()); // 3
        System.out.println(buf.readByte()); // 4

        log(buf); // 可以看出读指针往前偏移，之前读过的数据都废弃了

        // 做标记，读整数
        buf.markReaderIndex(); // 打一个标记
        System.out.println("做一个标记后 读取整数：" + buf.readInt()); // 读取一个整数 【四位】
        log(buf);
        buf.resetReaderIndex(); // 重置到上一个 打标记的位置
        System.out.println("重置到上一个标记后 读取整数：" + buf.readInt()); // 读取一个整数 【四位】
        log(buf);
    }

    /**
     * 打印工具方法
     *
     * @param buffer buffer
     */
    private static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf =
                new StringBuilder(rows * 80 * 2)
                        .append("read index:")
                        .append(buffer.readerIndex())
                        .append(" write index:")
                        .append(buffer.writerIndex())
                        .append(" capacity:")
                        .append(buffer.capacity())
                        .append(NEWLINE); // io.netty.util.internal.StringUtil.NEWLINE
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf);
    }
}
