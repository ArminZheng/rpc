package com.arminzheng.exercise.nio.boundary;

import com.arminzheng.exercise.nio.common.SelectorServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * 消息边界问题的 3 种思路：（本次为分隔符）
 *
 * <p>1 固定长度
 *
 * <p>2 分隔符（需要遍历所有）
 *
 * <p>3 标记 TLV (http2是LTV)
 *
 * <pre>
 * 本次为分隔符，主要处理「容量超出」问题：
 *
 * 操作：利用超出内存循环读取客户端数据：超出内存不操作，手动将原内存长度*2，或多次一直*2， 直到满足长度
 *
 * 程序里 '\n' 分隔 处理的缺点是：
 *      1. 主程序里 channel.read(buffer)读数据时，超出buffer分配内存长度, 将再次或多次触发读事件进行循环
 *      所以当发送数据超过分配内存长度时，第一次读取因为没有'\n'，将会「丢失」本次消息
 *
 * 解决思路：
 * 1.  首先将 channel.read(buffer) 的 buffer 抽取出来改为附件(使能方便一次性读取 客户端数据)
 * 2.  在每个客户端 SocketChannel 注册Selector时，第三参数 附件里加入我们开辟内存的ByteBuffer(这时buffer的生命周期将和SelectionKey一样)
 * 3.  然后在每次「读事件」里获取附件 attachment 强转回 ByteBuffer
 * 4.  子方法compact结束后，主方法对比 position==limit 检查是否超出内存，超出内存说明当前没有读取到，再次触发读事件循环
 * 5.  扩容：如果超出内存，开辟原内存字节长度改为两倍，进入下一次循环继续判断，直到满足长度，然后交给 read 处理</pre>
 *
 * @since 2022-05-29
 */
@Slf4j
public class BoundaryServer extends SelectorServer {

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {
        log.debug("register key: {}", acceptEventKey);
        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    // 分配 16字节的一个内存 来存放接收的数据
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    // 将一个 buffer 作为附件关联到 SelectionKey 上
                    SelectionKey scKey = sc.register(selector, 0, buffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 获取 accept 事件里 注册的 内存附件
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = channel.read(buffer);
                        if (read == -1) {
                            key.cancel();
                        } else {
                            // 如果内存空间写满了, 且不包含 '\n'
                            split(buffer);
                            /*
                             当发送数据 超出buffer分配内存长度 会两次或多次触发读事件循环
                             然后 每次循环 手动扩容一倍，如果还是超出buffer分配长度，则继续循环扩容
                            */
                            if (buffer.position() == buffer.limit()) {
                                System.out.println("buffer内存长度 " + buffer.capacity());
                                // 手动扩容
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                System.out.println("newBuffer内存长度 " + newBuffer.capacity());
                                buffer.flip(); // 老buffer切换读模式，好给newBuffer复制
                                key.attach(newBuffer.put(buffer)); // 复制+替换为新的附件参数
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();
                    }
                }
            }
        }
    }

    private static void split(ByteBuffer source) {
        source.flip();
        String output = "[ i  = %d , position = %d length  = %d ]\n";
        for (int i = 0; i < source.limit(); i++) {
            byte b = source.get(i);
            if (b == '\n') {
                int position = source.position();
                int length = i + 1 - position;
                System.out.printf(output, i, position, length); // 完整 消息 存入 新 buffer
                ByteBuffer target = ByteBuffer.allocate(length);
                System.out.print("position=[");
                // 从 source 读 ，写入 target
                for (int j = 0; j < length; j++) {
                    target.put(source.get()); // 每一次 get 时， position++
                    System.out.print(source.position() + ",");
                }
                debugAll(target);
            }
        }
        source.compact(); // 未读完部分向前压缩
    }
}
