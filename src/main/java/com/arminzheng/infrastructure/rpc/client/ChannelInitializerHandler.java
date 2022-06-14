package com.arminzheng.infrastructure.rpc.client;

import com.arminzheng.infrastructure.rpc.handler.RpcResponseMessageHandler;
import com.arminzheng.infrastructure.rpc.handler.codec.MessageCodecSharable;
import com.arminzheng.infrastructure.rpc.handler.codec.ProtocolFrameDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SocketChannelInitial
 *
 * @author zy
 * @since 2022.06.14
 */
@Component
@RequiredArgsConstructor
public class ChannelInitializerHandler extends ChannelInitializer<SocketChannel> {
    private final LoggingHandler loggingHandler = new LoggingHandler();
    private final MessageCodecSharable messageCodec;
    private final RpcResponseMessageHandler rpcResponseMessageHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new ProtocolFrameDecoder());
        ch.pipeline().addLast(loggingHandler);
        ch.pipeline().addLast(messageCodec);
        ch.pipeline().addLast(rpcResponseMessageHandler);
    }
}
