package com.arminzheng.client;

import com.arminzheng.infrastructure.rpc.handler.RpcResponseMessageHandler;
import com.arminzheng.infrastructure.rpc.handler.codec.MessageCodecSharable;
import com.arminzheng.infrastructure.rpc.handler.codec.ProtocolFrameDecoder;
import com.arminzheng.infrastructure.rpc.message.RpcRequestMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务调用端原型
 *
 * @author zy
 */
@Slf4j
class RpcPrototype {

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        RpcResponseMessageHandler RPC_RESPONSE_HANDLER = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtocolFrameDecoder()); // 【使用 asm包方法】
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(MESSAGE_CODEC);
                            ch.pipeline().addLast(RPC_RESPONSE_HANDLER);
                        }
                    });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.writeAndFlush(
                            new RpcRequestMessage(
                                    1,
                                    "com.arminzheng.service.HelloService",
                                    "sayHello",
                                    String.class,
                                    new Class[] {String.class},
                                    new Object[] {"helloworld!"}))
                    .addListener(
                            promise -> {
                                if (!promise.isSuccess()) {
                                    // 发送不成功  【打印错误信息】
                                    Throwable cause = promise.cause();
                                    log.error("error : ", cause);
                                }
                            });
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
