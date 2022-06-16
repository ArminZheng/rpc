package com.arminzheng.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Client
 *
 * @author zy
 * @version 2022/6/3
 */
@Slf4j
public class RedisClient {

    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap client = new Bootstrap();
            client.channel(NioSocketChannel.class);
            client.group(worker);
            client.handler(new ChannelInitial());
            ChannelFuture future = client.connect("localhost", 6379).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }
    }

    static class ChannelInitial extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LoggingHandler());
            pipeline.addLast(new MyChannelInboundHandlerAdapter());
        }

        private static class MyChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                // 首先把整个 redis命令 看成一个数组 [set, name, redis]
                final byte[] LINE = {'\r', '\n'}; // equal {13,10};
                ByteBuf buffer = ctx.alloc().buffer(); // allocate 分配 调拨
                buffer.writeBytes("*3".getBytes()).writeBytes(LINE); // 数组个数
                buffer.writeBytes("$3".getBytes()).writeBytes(LINE); // key命令长度
                buffer.writeBytes("set".getBytes()).writeBytes(LINE);
                buffer.writeBytes("$4".getBytes()).writeBytes(LINE); // key值 内容长度
                buffer.writeBytes("name".getBytes()).writeBytes(LINE);
                buffer.writeBytes("$5".getBytes()).writeBytes(LINE); // value值 内容长度
                buffer.writeBytes("redis".getBytes()).writeBytes(LINE);
                ctx.writeAndFlush(buffer);
                super.channelActive(ctx);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                ByteBuf buf = (ByteBuf) msg;
                log.info("redis返回结果: {}", buf.toString(StandardCharsets.UTF_8));
                super.channelRead(ctx, msg);
            }
        }
    }
}
