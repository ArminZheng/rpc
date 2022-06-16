package com.arminzheng.eventloop;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * EventLoopGroup 事件循环组
 *
 * <p>几个方法 next() next().submit() next().scheduleAtFixedRate()
 *
 * @author zy
 * @since 2022
 */
@Slf4j
public class EventLoopGroupTest {

    public static void main(String[] args) {
        // NioEventLoopGroup 获取 cpu个数
        final int i = NettyRuntime.availableProcessors();
        System.out.println(i);

        /*
        1. 创建 事件循环组
            未指定参数的话，会默认去读取系统配置 io.netty.eventLoopThreads
            最后使用 cpu核心数*2 创建线程 兜底
          Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
         */
        // NioEventLoopGroup:【 io 事件】普通任务，定时任务
        EventLoopGroup superIoEvent = new NioEventLoopGroup(2);
        // DefaultEventLoopGroup: 普通任务，定时任务
        EventLoopGroup normalTask = new DefaultEventLoopGroup();

        // 2. 获取下一个 循环对象 （返回一个 EventLoop：能处理队列任务的单线程池）
        System.out.println(superIoEvent.next()); // 1 io.netty.channel.nio.NioEventLoop@6d00a15d
        System.out.println(superIoEvent.next()); // 2 io.netty.channel.nio.NioEventLoop@51efea79
        System.out.println(superIoEvent.next()); // 1
        System.out.println(superIoEvent.next()); // 2
        System.out.println(superIoEvent.next()); // 1

        /*
        3. 普通任务 submit
            提交到 事件循环组 中, 让 某一个 事件循环对象(EventLoop) 去执行
            作用：譬如, 某件耗时的工作交给子线程完成
         */
        EventLoop eventLoop = superIoEvent.next();
        eventLoop.submit(
                () -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.debug("third 1️⃣子线程运行。。。");
                });

        log.debug("first 2️⃣主线程 运行。。。");

        eventLoop.execute(() -> System.out.println("hello world"));
        /*
        4. 定时任务 schedule[AtFixedRate]
            初始延时事件0
            间隔时间1
            时间单位 TimeUnit.SECONDS
         */
        EventLoop eventLoop2 = superIoEvent.next();
        eventLoop2.scheduleAtFixedRate(
                () -> log.debug("second 3️⃣子线程运行......"), 0, 3, TimeUnit.SECONDS);
    }
}
