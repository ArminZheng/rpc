package com.arminzheng.exercise.nio.common;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * 使用 nio 来理解 非阻塞模式 (使用单线程)
 *
 * <pre>
 * 缺点：
 *     while一直空转 非常消耗cpu性能</pre>
 */
@Slf4j
public class NonBlockServer {

    @SuppressWarnings({"InfiniteLoopStatement", "resource"})
    public static void main(String[] args) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 设为非阻塞模式，#accept 就不会阻塞了「没有就返回一个 null」
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8888));
        ArrayList<SocketChannel> channels = new ArrayList<>();
        while (true) {
            SocketChannel sc = ssc.accept();
            if (sc != null) {

                // 设置 channel 为非阻塞模式，#read 就不会阻塞「没有就返回 0」
                sc.configureBlocking(false);
                channels.add(sc);
            }
            for (SocketChannel channel : channels) {
                int read = channel.read(buffer); // 非阻塞了
                if (read > 0) {
                    log.debug("before read -  {}", channel);
                    buffer.flip();
                    debugAll(buffer);
                    buffer.clear();
                    log.debug("after read -  {}", channel);
                }
            }
        }
    }
}
