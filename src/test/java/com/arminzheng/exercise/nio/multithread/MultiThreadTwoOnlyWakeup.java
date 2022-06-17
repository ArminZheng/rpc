package com.arminzheng.exercise.nio.multithread;

import com.arminzheng.exercise.nio.common.SelectorServer;
import com.arminzheng.exercise.nio.multithread.worker.Worker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 多线程 优化前面的案例
 *
 * <p>第二部分： 开一个分线程 不使用 "线程安全队列"，进行分离 测试
 *
 * <p>这里 与 MultiThreadServer 类不同
 *
 * <p>未使用 ConcurrentLinkedQueue 线程安全队列 来 ( 把注册程序 以任务形式 加到 队列 让 子线程 执行 )\
 *
 * <p>仅仅 在 内部类register里 (1加入唤醒子线程 2再进行 读写通道 sc 注册到 子线程的selector)
 */
@Slf4j
public class MultiThreadTwoOnlyWakeup extends SelectorServer {

    public static void main(String[] args) throws IOException {
        // 创建主线程 命名为 boss
        Thread.currentThread().setName("boss");
        // 创建固定数量的 worker 并初始化
        final Worker worker = new Worker("worker-0");

        doubleSelectorLoop(
                key -> {
                    if (key.isAcceptable()) {
                        // 与客户端  读写通道
                        final SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        log.debug("RemoteAddress is {}", sc.getRemoteAddress());
                        worker.register1(sc); // 这里将开启多线程并和下面方法同步执行
                        log.debug("after  register... {}", sc.getRemoteAddress());
                    }
                });
    }
}
