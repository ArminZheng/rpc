package com.arminzheng.infrastructure.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.PreDestroy;

/**
 * rpc 服务消费端
 *
 * @author zy
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClientLauncher {

    private static final NioEventLoopGroup WORKER = new NioEventLoopGroup();

    private final ChannelInitialization channelInitialization;
    private volatile Channel channel;
    private Bootstrap bootstrap;

    public Channel channel() {
        if (channel != null && channel.isActive()) return channel;
        synchronized (ClientLauncher.class) {
            if (channel == null || !channel.isActive()) connect();
            return channel;
        }
    }

    private void init() {
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(WORKER);
        bootstrap.handler(channelInitialization);
    }

    private void connect() {
        if (bootstrap == null) init();
        connect("localhost", 8080);
    }

    public void connect(String inetHost, int inetPort) {
        disconnect();
        try {
            ChannelFuture future = bootstrap.connect(inetHost, inetPort).sync();
            // channel.closeFuture().addListener(future -> WORKER.shutdownGracefully()); // wrong
            if (future.isSuccess()) {
                channel = future.channel();
                log.info("connect server {}:{} success", inetHost, inetPort);
            }
        } catch (Exception e) {
            log.error("服务器连接失败: {}", e.getMessage());
            throw new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE, "服务端无法连接");
            // WORKER.shutdownGracefully(); // wrong
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
