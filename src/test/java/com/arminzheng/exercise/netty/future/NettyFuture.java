package com.arminzheng.exercise.netty.future;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * Future 里【 同步 和 异步 】获取结果
 *
 * <p>Future ：线程间 传递结果的容器
 *
 * <p>nettyFuture 继承自 jdk 的 Future
 *
 * @author zy
 */
@Slf4j
public class NettyFuture {

    public static void main(String[] args) {
        DefaultEventLoopGroup eventLoop = new DefaultEventLoopGroup();
        try {
            Future<Integer> future =
                    eventLoop.submit(
                            () -> {
                                Thread.sleep(2000);
                                log.debug("执行计算...");
                                return 666;
                            });
            log.debug("等待结果......");
            // 3. 主线程通过 future 来获取结果
            // 3.1 同步获取结果
            log.debug("同步获取结果 : {}", future.get()); // 阻塞方法，等待 子线程运行完毕

            // 3.2 异步获取结果 (监听+回调)
            future.addListener(
                    f -> {
                        final Integer j = (Integer) f.getNow(); // getNow 非阻塞方法，返回 Object
                        // 当前回调方法执行了，说明子线程已经完成了，get()方法也不再阻塞
                        log.debug("异步获取结果 : {}", j);
                    });
            log.debug("main 运行结束");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            eventLoop.shutdownGracefully(); // 因为是一个线程池资源，所以需要关闭
        }
    }
}
