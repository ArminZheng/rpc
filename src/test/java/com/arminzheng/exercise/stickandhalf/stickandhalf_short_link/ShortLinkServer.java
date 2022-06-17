package com.arminzheng.exercise.stickandhalf.stickandhalf_short_link;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 短链接 解决黏包
 *
 * <pre>
 * 使其发生半包：
 *
 * 服务端：
 *      调整netty的接受缓冲区 最小字节 16
 * 客户端：
 *      调整每次发送 大于 16字节</pre>
 */
@Slf4j
public class ShortLinkServer {

    void start() {
        final NioEventLoopGroup boss = new NioEventLoopGroup();
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            // 调整系统的接收缓冲区 (i.e. 滑动窗口) 设置10字节使其发生半包
            // option 针对全局的，整个服务器的一些选项配置
            // serverBootstrap.option(ChannelOption.SO_RCVBUF, 10); // 对发送方来说 SO_SENDBUF

            // 调整netty的 接受缓冲区(byteBuf)
            // childOption 针对每个channel 连接的选项配置
            serverBootstrap.childOption(
                    // 这里最小就是16，因为他总是取16的整数倍【注意还要调整客户端每次发送字节 > 16】
                    ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16));
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(
                    new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        }
                    });

            final ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("server error", e);

        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ShortLinkServer().start();
    }
}
