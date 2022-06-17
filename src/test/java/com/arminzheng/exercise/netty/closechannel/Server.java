package com.arminzheng.exercise.netty.closechannel;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 服务端
 *
 * @author zy
 */
@Slf4j
public class Server {

    public static void main(String[] args) {

        new ServerBootstrap()
                // 一个boss 一个worker
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                ch.pipeline()
                                        .addLast(
                                                "ooothread",
                                                new ChannelInboundHandlerAdapter() {
                                                    @Override
                                                    public void channelRead(
                                                            ChannelHandlerContext ctx, Object msg) {
                                                        // super.channelRead(ctx, msg);
                                                        ByteBuf buf = (ByteBuf) msg;
                                                        log.debug(
                                                                buf.toString(
                                                                        Charset.defaultCharset()));
                                                    }
                                                });
                            }
                        })
                .bind(8080);
    }
}
