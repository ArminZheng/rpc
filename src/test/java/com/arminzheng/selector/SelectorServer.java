package com.arminzheng.selector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Server
 *
 * @author zy
 * @version 2022/5/30
 */
@Slf4j
public class SelectorServer {

    protected static final ServerSocketChannel ssc;
    protected static final Selector selector;
    protected static final SelectionKey register;

    static {
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(8888));
            selector = Selector.open();
            register = ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    public static void main(String[] args) throws IOException {
        log.info("registry selection key event is {}", register);
        while (true) {
            selector.select();
            if (false) { // Selector#select 的其他用法
                // 等待 1000ms 就放行一次 netty
                selector.select(1000);
                // 不阻塞 直接放行
                selector.selectNow();
            }
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey activeKey = keys.next();
                keys.remove();
                if (activeKey.isAcceptable()) { // 仅注册
                    // SelectableChannel channel1 = activeKey.channel();
                    log.info("current key event accept is {}", activeKey == register); // true
                    SocketChannel channel = ssc.accept();
                    channel.configureBlocking(false);
                    SelectionKey registerKey = channel.register(selector, SelectionKey.OP_READ);

                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < 3000000; i++) {
                        builder.append("s");
                    }
                    ByteBuffer byteBuffer = Charset.defaultCharset().encode(builder.toString());
                    /* 方法一：写入时死循环，其他连接被hang住了
                    while (byteBuffer.hasRemaining()) {
                        int writeCount = channel.write(byteBuffer);
                        System.out.println(writeCount);
                    } */

                    // 方法二：先写入一次，后续再关注可写事件
                    //      1. 写入一次
                    int writeCount = channel.write(byteBuffer);
                    System.out.println(writeCount);

                    //      2. 判断是否还有剩余
                    if (byteBuffer.hasRemaining()) {
                        // channel.register(selector, SelectionKey.OP_WRITE); // 这里会被覆盖
                        // registerKey.interestOps(SelectionKey.OP_WRITE); // 这里会被覆盖
                        // 下面这种方式就能合并了 ( |、+ 都行)
                        registerKey.interestOps(SelectionKey.OP_WRITE | registerKey.interestOps());
                        // 还需要添加附件
                        registerKey.attach(byteBuffer);
                    }
                } else if (activeKey.isReadable()) { // 仅读取之前注册过的 read 事件
                    log.info("current key event read is {}", activeKey == register); // false
                    try {
                        SocketChannel channel = (SocketChannel) activeKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = channel.read(buffer);
                        if (read == -1) {
                            log.info("cancel normal");
                            activeKey.cancel();
                        } else {
                            buffer.flip();
                            // debugAll(buffer);
                            System.out.println(Charset.defaultCharset().decode(buffer));
                            buffer.compact();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.info("cancel cause exception!");
                        activeKey.cancel();
                    }
                } else if (activeKey.isWritable()) { // 必须进行短路，不然会报 CancelledKeyException
                    log.info("current key event write is {}", activeKey == register);
                    // 3. 取到附件
                    ByteBuffer attachment = (ByteBuffer) activeKey.attachment();
                    // 4. 拿到 channel
                    SocketChannel channel = (SocketChannel) activeKey.channel();
                    // 5. 第 2-n 次写
                    int writeCount = channel.write(attachment);
                    System.out.println(writeCount);

                    // 6. 注意 记得清空
                    if (!attachment.hasRemaining()) {
                        log.info(" clean ");
                        activeKey.attach(null);
                        // activeKey.cancel(); // 不能使用 cancel 因为还绑定了其他的事件
                        // ( ^、- 都行)
                        activeKey.interestOps(activeKey.interestOps() ^ SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }
}
