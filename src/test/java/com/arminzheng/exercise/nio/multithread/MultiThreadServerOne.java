package com.arminzheng.exercise.nio.multithread;

import com.arminzheng.exercise.nio.common.SelectorServer;
import com.arminzheng.exercise.nio.multithread.worker.QueueWorker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 第一部分： 开一个分线程 ,使用 "线程安全队列"，进行分离 测试
 *
 * <p>多线程 的selector应用
 *
 * <p>分工操作 （boss 和 worker 都有自己独立的线程和 selector）
 *
 * <p>  Boss : 负责接待 连接操作
 *
 * <p>  worker : 负责读写，每个work负责 一或多个SockerChannel读写事件
 *
 * <p>难点：解决 boss (register ) 和 worker线程里 ( select() )前后顺序 问题
 *
 * <p>    \t  register 先执行 还可以， select 先执行的话 将阻塞住
 *
 * <p>核心：模仿 netty的 操作步骤
 */
@Slf4j
public class MultiThreadServerOne extends SelectorServer {

    public static void main(String[] args) throws IOException {
        // 创建主线程 命名为 boss
        Thread.currentThread().setName("boss");
        // 创建固定数量的 worker 并初始化
        final QueueWorker worker = new QueueWorker("worker-0");

        doubleSelectorLoop(
                key -> {
                    if (key.isAcceptable()) {
                        // 客户端 读写通道
                        final SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        log.debug("RemoteAddress is {}", sc.getRemoteAddress());
                        /*
                         * 注意：这里必须先 clientChannel.register关注 OP_READ 事件，然后 再 静态内部类 里执行 selector.select() 才能顺利完成读取
                         *
                         * select() 先执行的话，会将 register阻塞住
                         */
                        worker.register1(sc);
                        log.debug("after register... {}", sc.getRemoteAddress());
                    }
                });
    }

}
