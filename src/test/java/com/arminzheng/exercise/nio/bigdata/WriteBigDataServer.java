package com.arminzheng.exercise.nio.bigdata;

import com.arminzheng.exercise.nio.common.SelectorServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * 通过关注 write 写事件, 非阻塞式发送大量数据 避免长轮训影响其他 channel
 *
 * @author zy
 */
public class WriteBigDataServer extends SelectorServer {

    @SuppressWarnings({"resource", "InfiniteLoopStatement"})
    public static void main(String[] args) throws IOException {
        int count = 0;
        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator(); // 事件集合迭代器
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove(); // 拿到须手动移除
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false); // 拿到客服端channel 设为非阻塞
                    SelectionKey scKey = sc.register(selector, 0, null); // 为客户端注册关注事件
                    // 构造大量的 buffer 写入数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 30000000; i++) sb.append("a");
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    /*
                    Background: 服务端发送「大量字节」给客户端时，网络资源有限
                    通过 write缓冲区容易将channel写满，导致一次写不完，就会多次写，这样导致其他客户端连接的 Channel 被阻塞
                        阻塞式：
                            while(buffer.hasRemaining()) { // 是否有剩余字节
                                // 底层是 发送缓冲区 控制一次发多少的
                                int write = sc.write(buffer); // 实际写入 字节数
                                System.out.println(write);
                                count += write;
                            }
                        非阻塞式：将 上面的 while循环 处理成 多个可写事件
                            1. 先写一次 ，没写完有剩余字节 将关注写事件，将未写完数据挂到 scKey 的附件
                            2. 等待下一次 socket 缓冲区可写了，得到可写事件，再继续向channel发送数据。
                            3. 下一轮进入 selector.select() 将又一次触发写事件，这个事件会频繁触发。
                            4. 在写事件内的操作拿到附件，继续写。
                            5. 如果写完了，没有可写内容了，就置空挂载的数据，取消可写事件关注，buffer内存将得到释放 */
                    int write = sc.write(buffer);
                    count += write;
                    System.out.println("初次写入字节数 " + write + " 目前总数 " + count);
                    // 还有剩余 -> 关注可写事件 + 挂载 buffer数据 to key's attach
                    if (buffer.hasRemaining()) {
                        // 关注 可写事件, 并不破坏原来关注的事件  读1 + 写4 (读1 | 写4)
                        scKey.interestOps(scKey.interestOps() | SelectionKey.OP_WRITE);
                        // 未写完的数据 挂到 scKey 里
                        scKey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(buffer);
                    count += write;
                    System.out.println("当前写入字节数 " + write + " 目前总数 " + count);
                    if (!buffer.hasRemaining()) { // done!
                        key.attach(null); // 取消挂载的数据
                        // 取消可写事件关注 -, ^(加减号)
                        // in this statement must have written key 1 ^ 1 to 0, but not 1 ^ 0 to 1
                        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }
}
