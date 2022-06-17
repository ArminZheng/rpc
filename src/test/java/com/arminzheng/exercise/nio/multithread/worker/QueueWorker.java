package com.arminzheng.exercise.nio.multithread.worker;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * 有线程安全队列的 worker
 *
 * <p>有独立的线程 和 自己的 selector
 */
@Slf4j
public class QueueWorker extends Worker {

    /**
     * 创建一个线程安全的队列 (让两个线程之间传输数据)
     *
     * <p>目的：把注册的 channel 传过来
     */
    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public QueueWorker(String name) {
        super(name);
    }

    @Override
    public void register1(SocketChannel sc) throws IOException {
        register0();
        // boss 线程里的任务添加到队列，但是未执行
        queue.add(
                () -> {
                    try {
                        // 任务内容：将主线程的「读写通道」注册到子线程的selector，交给子线程管理
                        sc.register(selector, SelectionKey.OP_READ, null);
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                    }
                });
        selector.wakeup(); // 唤醒一次 select 方法；关注 read 事件
    }

    /**
     * 这 先 select() 因为在boss线程里面已经 强行wakeup() 了 所以首次不阻塞
     *
     * <p>再 register() 先注册 关注了read事件，首次 有事件将继续，没事件将到下个循环
     */
    @SuppressWarnings({"InfiniteLoopStatement", "resource"})
    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                // 从 队列里拿出「任务」并执行
                final Runnable task = queue.poll();
                if (task != null) {
                    // 执行任务（非开启新线程）
                    task.run();
                }
                // 首次到这里可能还没任务事件，但是 读事件 已经注册好了(就可以 关注 客户端是否传数据了)
                final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    final SelectionKey key = iterator.next();
                    iterator.remove();
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
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
