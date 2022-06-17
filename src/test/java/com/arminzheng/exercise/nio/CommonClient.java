package com.arminzheng.exercise.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * 客户端
 *
 * <p>单线程不能处理多个客户端，阻塞模式下两个阻塞方法相互影响
 *
 * @since 2022-05-29
 */
public class CommonClient {

    protected static final SocketChannel sc;

    static {
        try { // init SocketChannel
            //noinspection
            sc = SocketChannel.open();
            // 指定要连接的 服务器 和 端口号
            sc.connect(new InetSocketAddress("localhost", 8888));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        // sc.write(Charset.defaultCharset.encode("hello world"));
        //noinspection ResultOfMethodCallIgnored
        System.in.read(); // 阻塞方法，等待控制台输入
    }
}
