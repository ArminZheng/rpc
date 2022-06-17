package com.arminzheng.exercise.nio.multithread;

import com.arminzheng.exercise.nio.common.SelectorServer;
import com.arminzheng.exercise.nio.multithread.worker.Worker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 阻塞现象
 *
 * <p>这里与 MultiThreadServer类不同，未使用 ConcurrentLinkedQueue 线程安全队列，来把注册程序以任务形式加到队列让子线程执行
 */
@Slf4j
public class BlockMultiThreadServer extends SelectorServer {

    public static void main(String[] args) throws IOException {
        // 创建主线程命名为 boss
        Thread.currentThread().setName("boss");
        // 创建固定数量的 worker 并初始化
        final Worker worker = new Worker("worker-0");

        doubleSelectorLoop(
                key -> {
                    if (key.isAcceptable()) {
                        // 客户端 读写通道
                        final SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        log.debug("RemoteAddress is {}", sc.getRemoteAddress());
                        /*
                        注意：这里必须先 sc.register 关注read事件，然后再执行 selector.select() 才能顺利完成读取

                        select() 先执行的话，会将 register阻塞住 */
                        worker.register0(); // 这里将开启多线程并和下面方法同步执行

                        // Thread.sleep(1000);// 如果 暂停1秒 将 不会 先 关注 read事件
                        System.out.println("睡眠后  。。。。");

                        // 关联 worker 里的 选择器 selector
                        // 这里的 客户端读写通道  注册到 内部类worker里的 selector
                        sc.register(worker.selector, SelectionKey.OP_READ, null);
                        System.out.println(" sc.register后 。。。。");
                        log.debug("after register... {}", sc.getRemoteAddress());
                    }
                });
    }
}
