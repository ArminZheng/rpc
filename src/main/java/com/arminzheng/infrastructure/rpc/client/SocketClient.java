package com.arminzheng.infrastructure.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * rpc 服务消费端
 *
 * @author zy
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SocketClient {

    private static final NioEventLoopGroup WORKER = new NioEventLoopGroup();

    private final ChannelInitializerHandler channelInitializerHandler;
    private volatile Channel channel;
    private Bootstrap bootstrap;

    public Channel channel() {
        if (channel != null && channel.isActive()) return channel;
        synchronized (SocketClient.class) {
            if (channel == null || !channel.isActive()) connect();
            return channel;
        }
    }

    private void init() {
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(WORKER);
        bootstrap.handler(channelInitializerHandler);
    }

    private void connect() {
        if (bootstrap == null) init();
        connect("localhost", 8080);
    }

    public void connect(String inetHost, int inetPort) {
        disconnect();
        try {
            ChannelFuture future = bootstrap.connect(inetHost, inetPort).sync();
            if (future.isSuccess()) {
                channel = future.channel();
                log.info("connect server {}:{} success", inetHost, inetPort);
                // channel.closeFuture().addListener(future -> WORKER.shutdownGracefully()); //
                // wrong
            }
        } catch (Exception e) {
            log.error("client error", e);
            WORKER.shutdownGracefully();
        }
    }

    public void disconnect() {
        if (channel != null && channel.isActive()) channel.close();
    }

    @PreDestroy
    public void destroy() {
        disconnect();
        WORKER.shutdownGracefully();
    }
}
