package com.arminzheng.selector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Client
 *
 * @author zy
 * @version 2022/5/30
 */
@Slf4j
public class NioClient {
    public static void main(String[] args) {
        System.out.println(
                Runtime.getRuntime().availableProcessors()); // docker 下无法获取到虚拟机上的核心数，jdk10 才解决
        try (SocketChannel channel = SocketChannel.open()) {
            // 如果不想被转译为ip地址 使用 InetSocketAddress.createUnresolved("example.com", 8080);
            channel.connect(new InetSocketAddress(8888));
            channel.write(Charset.defaultCharset().encode("hello world"));
            System.out.println("hello hang");
            int count = 0;
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            //noinspection InfiniteLoopStatement
            while (true) {
                count += channel.read(buffer);
                System.out.println("count = " + count);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
