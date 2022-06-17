package com.arminzheng.exercise.nio.multithread.worker;

import com.arminzheng.exercise.nio.common.SelectorServer;
import com.arminzheng.exercise.nio.multithread.MultiThreadTwoOnlyWakeup;
import com.arminzheng.exercise.nio.multithread.BlockMultiThreadServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * 内部类 worker
 *
 * <p>有独立的线程
 *
 * <p>和自己的 selector
 */
@Slf4j
public class Worker implements Runnable {

    public Selector selector;
    protected final String name; // 每个 selector的名字

    /**
     * volatile声明变量的值可能随时会别的线程修改 修饰的变量会强制将修改的值 立即写入主存 主存中值的更新会使缓存中的值失效
     *
     * <p>(非volatile变量不具备这样的特性，非volatile变量的值会被缓存， 线程A更新了这个值，线程B读取这个变量的值时可能读到的并不是是线程A更新后的值)
     *
     * <p>volatile具有 可见性 有序性，不具有 原子性
     */
    protected volatile boolean start = false; // 不重复创建, 设置一个初始化变量

    public Worker(String name) {
        this.name = name;
    }

    /**
     * 初始化线程 & selector，并启动线程
     *
     * <p>一个 worker一个线程，不重复创建
     *
     * @see BlockMultiThreadServer TestServer1类 不用唤醒selector
     */
    public void register0() throws IOException {
        if (!start) { // 只执行一遍
            Thread thread = new Thread(this, name);
            // 创建 selector 后，才能 start 线程。不然 selector 会报空指针
            selector = Selector.open();
            thread.start();
            start = true;
        }
    }

    /**
     * 在本方法内进行注册
     *
     * @see MultiThreadTwoOnlyWakeup
     * @param sc SocketChannel
     * @throws IOException Selector、SocketChannel 抛出的异常
     */
    public void register1(SocketChannel sc) throws IOException {
        register0();
        selector.wakeup(); // 唤醒 selector
        // 客户端读写通道  注册到 内部类worker里的 selector
        sc.register(selector, SelectionKey.OP_READ, null);
    }

    @SuppressWarnings("resource")
    @Override
    public void run() {
        try {
            SelectorServer.doubleSelectorLoop(
                    key -> {
                        if (key.isReadable()) {
                            try {
                                final ByteBuffer buffer = ByteBuffer.allocate(16);
                                final SocketChannel channel = (SocketChannel) key.channel();
                                log.debug("worker read remote is {}", channel.getRemoteAddress());
                                int read = channel.read(buffer);
                                if (read == -1) {
                                    log.error(" normal cancel ");
                                    key.cancel(); //  取消掉，让 SelectionKey 反向取消注册，并使 select() 阻塞
                                } else {
                                    buffer.flip();
                                    debugAll(buffer);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                log.error(" exception cancel ");
                                key.cancel(); //  取消掉，让 SelectionKey 反向取消注册，并使 select() 阻塞
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
