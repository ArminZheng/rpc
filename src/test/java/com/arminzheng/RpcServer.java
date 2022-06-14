package com.arminzheng;

import com.arminzheng.infrastructure.rpc.handler.RpcRequestMessageHandler;
import com.arminzheng.infrastructure.rpc.handler.codec.MessageCodecSharable;
import com.arminzheng.infrastructure.rpc.handler.codec.ProtocolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * rpc 服务提供端
 *
 * @author zy
 */
@Slf4j
class RpcServer {
    private static final NioEventLoopGroup BOSS = new NioEventLoopGroup();
    private static final NioEventLoopGroup WORKER = new NioEventLoopGroup();

    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
    private static final MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
    private static final RpcRequestMessageHandler RPC_REQUEST = new RpcRequestMessageHandler();

    private static final ChannelInitial CHANNEL_INITIAL = new ChannelInitial();

    public static void main(String[] args) {
        try {
            ServerBootstrap bs = new ServerBootstrap();
            bs.channel(NioServerSocketChannel.class);
            bs.group(BOSS, WORKER);
            bs.childHandler(CHANNEL_INITIAL);
            Channel channel = bs.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            BOSS.shutdownGracefully();
            WORKER.shutdownGracefully();
        }
    }

    static class ChannelInitial extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ch.pipeline().addLast(new ProtocolFrameDecoder());
            ch.pipeline().addLast(LOGGING_HANDLER);
            ch.pipeline().addLast(MESSAGE_CODEC);
            ch.pipeline().addLast(RPC_REQUEST);
        }
    }
}
