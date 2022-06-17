package com.arminzheng.exercise.nio.common;

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
 * 使用多路复用
 *
 * <pre>
 * 服务端
 *     这里用 selector 管理 多个 channel ( 可管理 ServerSocketChannel、SocketChannel )
 *
 *     多路复用 : 单线程可以配合 Selector 完成对多个 Channel 可读写事件的监控
 *
 *
 * SelectionKey  通过它可以知道事件 和 知道哪个channel通道
 * SelectionKey  四种事件：
 *        accept  -客户端连接请求触发 (服务端 事件)
 *        connect -服务端连接建立触发 (客户端 事件)
 *        read    -可读事件
 *        write   -可写事件
 *
 *  1）服务器连接通道 注册 到 selector 后
 *  2）将来 selector可以监听到所有事件
 *  3）如果这个事件发生了，将把这个 SelectionKey 放到 selectedKeys 集合里
 *  4）然后 遍历selectedKeys 集合，每拿出一个 SelectionKey 就知道发生了什么事件
 *
 *  SelectionKey.channel() 可以获取 channel,因为每个key都关联一个channel，可能是服务端或客户端的
 *
 *  一个 channel 仅能和一个 selector 绑定
 *
 *  Selector { List< SelectionKey > }   SelectionKey{ ServerSocketChannel }
 *
 *  Selector.select() 监听事件，详细的是：
 *      有未处理的未取消事件，不阻塞
 *      取消了 或 已处理了 则阻塞
 *      # 所以 事件要么处理 要么取消</pre>
 *
 * @since 2022-05-29
 */
@Slf4j
public class SelectorServer {
    /** 服务器对象通道 */
    protected static final ServerSocketChannel ssc;
    /** 管理多个channel */
    protected static final Selector selector;
    /** 可以知道 事件 和 对应 channel通道 */
    protected static final SelectionKey acceptEventKey;

    static {
        try {
            // 创建一个「服务器对象通道」
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false); // selector 必须工作在非阻塞模式下（使accept成非阻塞）
            // 绑定一个 监听端口
            ssc.bind(new InetSocketAddress(8888));
            // 1. 创建selector来管理多个channel
            selector = Selector.open();
            // 将 服务器连接通道 注册到 selector （ops：0 不关注任何事件）
            acceptEventKey = ssc.register(selector, 0, null);
            // 指明 SelectionKey 「绑定/感兴趣」的事件
            acceptEventKey.interestOps(SelectionKey.OP_ACCEPT);
            // 「简化」注册&关注连接事件 ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void doubleSelectorLoop(KeyLoop keyLoop) throws IOException {
        while (true) {
            // 3. 选择器，事件未发生时阻塞，有事件未处理或未取消时放行；
            // （里面包含 serverChannel 集合，事件发生如 accept() 之后内部会调用 Selector#wakeup 方法）
            // 注意⚠️：1.要么处理事件 2.要么取消事件; 因为 未处理 或 未取消事件时 不会阻塞而往下继续运行;
            selector.select();
            // 4. 处理事件
            //  selectKeys 内部包含所有发生的事件，譬如两个客户端连上了, 就会有两个key
            //  tips: 必须用迭代器才能在 遍历里删除元素
            // Selector#selectedKeys 是新发生的事件，和 Selector#keys 不一样
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                // 发生的事件集合
                // 每次迭代完 要移除掉，不然  可读事件 进来 循环时， 先判断肯定是ssc accept事件，
                // 但是此时没有连接事件(这个事件还是上一次的,事件不会自己删除)，所以在处理时 sc=channel.accept() 是null ，
                // 下面进一步处理时，就报空指针异常
                keys.remove(); // selectedKeys 里删除
                keyLoop.iterator(key);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        doubleSelectorLoop(
                key -> {
                    // 5. 根据 事件类型 处理
                    if (key.isAcceptable()) { // accept  -客户端连接请求触发 (服务端 事件)
                        log.debug("acceptKey: {}", key);
                        // 获取 事务绑定的 服务器对象通道
                        ServerSocketChannel currentServerChannel =
                                (ServerSocketChannel) key.channel();
                        // 获取 读写通道
                        // 【【【调用accept方法 就意味着把事件 处理掉了，或者 取消  key.cancel();】】】
                        SocketChannel clientChannel =
                                currentServerChannel.accept(); // 非阻塞式 没 eventKeys.remove() 会返回 null
                        clientChannel.configureBlocking(
                                false); // selector 必须工作在非阻塞模式下   影响 read 变成 非阻塞方法

                        // 读写通道 SocketChannel 注册到 selector 上
                        SelectionKey readEvent = clientChannel.register(selector, 0, null);
                        readEvent.interestOps(SelectionKey.OP_READ);
                        log.debug("bind readKey {} in accept event.", readEvent);

                        log.debug("SocketChannel sc : {}", clientChannel);
                    } else if (key.isReadable()) { // read    -可读事件
                        log.debug("readKey: {}", key);
                        /*
                        try 处理 客户端的 read事件(异常关闭触发) 的 read 异常
                           1. 不 cancel 取消的话，即使remove，下次还 select出来
                        */
                        try {
                            // SocketChannel才有 读权限
                            SocketChannel currentChannel = (SocketChannel) key.channel();
                            // 分配 16字节的一个内存 来 存放接收的数据
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            // 客户端 正常断开产生一个read事件，但是没数据， 返回的是 -1
                            int read = currentChannel.read(buffer);
                            if (read == -1) {
                                key.cancel(); //  取消掉，让 SelectionKey 反向取消注册，并使 select() 阻塞
                            } else {
                                buffer.flip();
                                System.out.println(Charset.defaultCharset().decode(buffer));
                                // debugAll(buffer);
                                buffer.clear();
                            }
                        } catch (IOException e) { // 要注意正常关闭，也需要 cancel 掉
                            e.printStackTrace();
                            key.cancel(); // 取消掉，让 select() 阻塞，发生事件.cancel() // 反注册掉
                        }
                    }
                });
    }

    public interface KeyLoop {
        void iterator(SelectionKey key) throws IOException;
    }
}
