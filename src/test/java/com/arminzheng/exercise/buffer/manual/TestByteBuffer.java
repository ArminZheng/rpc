package com.arminzheng.exercise.buffer.manual;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 循环读取文件
 *
 * <pre>
 * 1. allocate 分配内存
 * 2. read 从文件中 读取
 * 3. flip 切换读模式
 * 4. get  获取字节 并转字符 打印
 * 5. compact 或 clear 调整 position 和 limit 进行 写模式</pre>
 */
@Slf4j
public class TestByteBuffer {

    /**
     * 注意：文件可能很大，缓冲区不能跟随文件的大小设置的很大
     *
     * @param args CMD
     */
    public static void main(String[] args) {
        // FIleChannel: 数据的读写通道
        // 1. 输入输出流， 2. RandomAccessFIle 随机读写文件类
        try (FileInputStream fileInputStream = new FileInputStream("data.txt");
                FileChannel channel = fileInputStream.getChannel()) {
            // 准备缓冲区 存储读取数据
            ByteBuffer buffer = ByteBuffer.allocate(10); // 划分一个十个字节 的内存【单位：字节】
            while (true) {
                // 从 channel 里读取数据，准备 向 buffer 写入
                int len = channel.read(buffer); // 返回值：读到的实际字节数，-1则是没有数据了

                log.debug("此次读取到的字节长度是: {}", len);
                if (len == -1) break; // quit

                buffer.flip(); // 切换至 读模式 position指针指向开头，limit指向写入的最后位置 (内存长度)
                while (buffer.hasRemaining()) { // 检查是否有剩余的数据
                    byte b = buffer.get(); // 一次读一个字节
                    log.debug("实际字节 {}", (char) b); // 强转字符 并打印
                }
                // buffer.clear(); // 一次循环后  切换为写模式 复位 position limit 指针 (不动实际数据)
                buffer.compact(); // 从 上次未读完的地方 向前移动，并沿着 未读完数据的最后一位往后写
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
