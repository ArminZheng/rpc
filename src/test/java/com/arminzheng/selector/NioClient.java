package com.arminzheng.selector;

import lombok.Cleanup;
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
    public static void main(String[] args) throws IOException {
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("Docker下无法获取虚拟机上的核心数，在jdk10才解决");
        System.out.println("CPU核心数 = " + processors);
        @Cleanup SocketChannel channel = SocketChannel.open();
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
    }
}
