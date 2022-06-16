package com.arminzheng.eventloop;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class SplitEventLoopClient {

    public static void main(String[] args) throws InterruptedException, IOException {
        Bootstrap client = new Bootstrap(); // 1. 引导程序
        /* channelFuture （异步非阻塞 方法）
        一般带 Future 或 Promise 这些都是跟 异步方法 配套使用的； */
        final ChannelFuture channelFuture =
                client.group(new NioEventLoopGroup()) // 2. 添加 EventLoop 事件循环组
                        .channel(NioSocketChannel.class) // 3. 选择 客户端 channel 实现 客户端实现类 class
                        .handler(
                                new ChannelInitializer<NioSocketChannel>() { // 4. 添加处理器
                                    @Override
                                    protected void initChannel(NioSocketChannel ch) { // 连接建立 后 调用
                                        ch.pipeline().addLast(new StringEncoder());
                                    }
                                })
                        /*
                        5. 连接到 服务器
                            channelFuture : 异步非阻塞方法 main 发起调用，
                            真正 connect 连接的 是 NioEventLoopGroup 下某个 EventLoop某个线程
                        */
                        // 如果不想被转译为ip地址 使用 InetSocketAddress.createUnresolved("example.com", 8080);
                        .connect(new InetSocketAddress("localhost", 8080));

        // 6' 同步处理结果 主线程 等待，结束后主线程拿结果
        channelFuture.sync(); // 【阻塞等待中...】 直到nio线程 连接建立完毕  再继续往下执行

        // 无阻塞的向下运行， 如果没有上面的 sync() 方法 的话，这里获取的 channel 将是未建立好连接的
        final Channel channel = channelFuture.channel();
        log.debug("未使用sync()方法的 channel = {}", channel);
        // c.a.n.b.B_EventLoopClient - 未使用sync()方法的 channel = [id: 0x78cc9e49]
        log.debug("使用sync()方法的 channel = {}", channel);
        // channel = [id: 0xdc8e5146, L:/127.0.0.1:9361 - R:localhost/127.0.0.1:8080]

        channel.writeAndFlush("sync hello world");

        // 6'' 使用 addListener(回调对象) 异步处理结果
        channelFuture.addListener(
                (ChannelFutureListener)
                        future -> { // nio 线程 建立好之后，会调用 此方法
                            final Channel channel1 = future.channel();
                            log.debug("用addListener() 异步处理结果后 得到的 channel = {}", channel1);
                            /* channel = [id: 0x8ed66f33, L:/127.0.0.1:10222 - R:localhost/127.0.0.1:8080] */
                            channel1.writeAndFlush("线程建立好后的回调对象addListener异步处理结果");
                        });

        // 阻塞
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
    }
}
