package com.arminzheng.exercise.netty.future;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * Promise 继承自 Future, 属于主动创建的[结果容器]
 *
 * <p>promise.setSuccess(80)/ promise.setFailure(e) / promise.get()
 *
 * @author zy
 */
@Slf4j
public class NettyPromise {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 准备 EventLoop 对象
        final EventLoop eventLoop = new NioEventLoopGroup().next();
        // 2. 主动创建 promise，结果容器
        final DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        // 3. 任意一线程 执行计算，计算完毕后向 promise 填充结果
        new Thread(
                        () -> {
                            try {
                                Thread.sleep(2000);
                                int i = 1 / 0;
                                promise.setSuccess(80); // 设置成功结果
                            } catch (Exception e) {
                                e.printStackTrace();
                                promise.setFailure(e); // 设置失败结果
                            }
                        })
                .start();
        // 4. promise.get() 主线程接收 结果
        log.debug("结果是： {}", promise.get()); // get() 阻塞方法(同步)
    }
}
