package com.arminzheng.exercise.nio.multithread;

import com.arminzheng.exercise.nio.common.SelectorServer;
import com.arminzheng.exercise.nio.multithread.worker.QueueWorker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 第三部分：开多个worker线程使用 "线程安全队列"，进行分离测试
 *
 * <pre>
 * 这里是 MultiThreadServer 的升级版 ， 多 worker 使用数组保存多个内部类对象
 *
 * 创建固定数量的 worker 并初始化 数组的线程数 至少设置为 CPU 的核心数 得到 CPU核心数 ：
 *         Runtime.getRuntime().availableProcessors() 【注意拿的不是容器申请的核心数，而是物理的CPU核心数，直到 JDK10才修复】
 *
 *         建议 手工指定下 更好 根据实际情况，如果是 CPU 密集型运算, 线程数 设为 CPU核心数 如果 IO频繁， CPU用的少 ，参考 阿姆达尔定律 ，根据 IO跟
 *         计算的比例 来确认 多少线程 ，一般是 大于 CPU核心的</pre>
 */
@Slf4j
public class MultiThreadServerThreeMultiWorker extends SelectorServer {

    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        /*

        */
        // 创建固定数量的 worker 并初始化 数组的线程数 至少设置为 CPU 的核心数 得到 CPU核心数
        final QueueWorker[] workers = new QueueWorker[2];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new QueueWorker("worker-" + i);
        }
        // 计数器
        final AtomicInteger index = new AtomicInteger(); // 初始值 是 0
        doubleSelectorLoop(
                key -> {
                    // 连接事件 后 的 操作 内容
                    if (key.isAcceptable()) {
                        // 与客户端  读写通道
                        final SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        log.debug("RemoteAddress is {}", sc.getRemoteAddress());
                        /*
                        注意：这里必须先 sc.register关注 OP_READ 事件，然后 再 静态内部类 里执行 selector.select()
                        才能顺利完成读取, select() 先执行的话，会将 register阻塞住 */

                        // 使用 round robin 轮流交替使用负载均衡的方法 index.getAndIncrement 获取索引并自增一次
                        workers[index.getAndIncrement() % workers.length].register1(sc);
                        log.debug("after register... {}", sc.getRemoteAddress());
                    }
                });
    }
}
