package com.arminzheng.exercise.filespaths;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 两个 channel 之间 传输数据
 *
 * @since 5/29/2022
 */
@Slf4j
public class TestFileChannelTransferTo {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try (FileInputStream inputStream = new FileInputStream("from.txt");
                FileOutputStream outputStream = new FileOutputStream("to.txt")) {
            final FileChannel from = inputStream.getChannel();
            final FileChannel to = outputStream.getChannel();
            // 剩余 未传输的 字节数
            long size = from.size();
            // Question: 上限：最大一次传输 2G 数据，超出不会被传输

            // from.transferTo(0, from.size(), to); // before: FileChannel#transferTo

            for (long remainder = size; remainder > 0; ) { // after: use for when bigger 2 GB
                long position = size - remainder; // 全部大小 - 剩余字节树
                System.out.println("position:  " + position + " remainder:" + remainder);
                // 比 输入输出流 效率高，底层使用操作系统的 零拷贝 进行优化
                remainder -= from.transferTo(position, remainder, to); // transferTo 返回实际传输的字节数
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("---time cost: " + (System.currentTimeMillis() - start) + " 毫秒---");
    }
}
