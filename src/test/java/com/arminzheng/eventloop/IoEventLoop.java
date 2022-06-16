package com.arminzheng.eventloop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * EventLoop 处理 io事件 1. EventLoop 职责细分，创建 一个 boss EventLoop 和 两个 worker EventLoop 创建4 个客户端，会是 两个
 * worker 各负责两个 客户端channel 2. DefaultEventLoopGroup 继续细分， 创建独立的 一个 EventLoop 来专门负责 耗时长的 任务 创建4
 * 个客户端，每个channel 都会绑定 NIOEvent 和 DefaultEvent 18:28:38 [DEBUG] [nioEventLoopGroup-4-1]
 * c.a.n.b.B_EventLoopIO - sync---aaaa 18:28:38 [DEBUG] [defaultEventLoopGroup-2-11]
 * c.a.n.b.B_EventLoopIO - sync---aaaa
 *
 * <p>18:30:25 [DEBUG] [nioEventLoopGroup-4-2] c.a.n.b.B_EventLoopIO - sync---bbbb 18:30:25 [DEBUG]
 * [defaultEventLoopGroup-2-12] c.a.n.b.B_EventLoopIO - sync---bbbb
 *
 * <p>18:30:40 [DEBUG] [nioEventLoopGroup-4-1] c.a.n.b.B_EventLoopIO - sync---1111 18:30:40 [DEBUG]
 * [defaultEventLoopGroup-2-13] c.a.n.b.B_EventLoopIO - sync---1111
 *
 * <p>18:31:19 [DEBUG] [nioEventLoopGroup-4-2] c.a.n.b.B_EventLoopIO - sync---2222 18:31:19 [DEBUG]
 * [defaultEventLoopGroup-2-14] c.a.n.b.B_EventLoopIO - sync---2222
 */
@Slf4j
public class IoEventLoop {

    public static void main(String[] args) {
        ServerBootstrap server = new ServerBootstrap();
        // 不需要处理 IO事件 的 eventLoop  【DefaultEventLoopGroup 根据CPU*2 创建多个EventLoopIO】
        final DefaultEventLoopGroup defEventLoopGroup = new DefaultEventLoopGroup();
        /*
        服务端的 EventLoop 职责划分：
            group(boss, worker)：
                boss: ServerSocketChannel 只会与 EventLoopGroup 的一个 EventLoop 绑定，所以只设置 "1"
                worker: 根据需求设置
         */
        server.group(new NioEventLoopGroup(), new NioEventLoopGroup(2));
        server.channel(NioServerSocketChannel.class);
        server.childHandler(
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(
                                        "handler1",
                                        new ChannelInboundHandlerAdapter() {
                                            @Override
                                            public void channelRead(
                                                    ChannelHandlerContext ctx, Object msg) {
                                                // super.channelRead(ctx, msg);
                                                ByteBuf buf = (ByteBuf) msg;
                                                log.debug(buf.toString(Charset.defaultCharset()));
                                                // 让消息 传递给 下一个 handler
                                                ctx.fireChannelRead(msg);
                                            }
                                        });
                                pipeline.addLast(
                                        defEventLoopGroup,
                                        "handler2",
                                        new ChannelInboundHandlerAdapter() {
                                            @Override
                                            public void channelRead(
                                                    ChannelHandlerContext ctx, Object msg) {
                                                // super.channelRead(ctx, msg);
                                                ByteBuf buf = (ByteBuf) msg;
                                                log.debug(buf.toString(Charset.defaultCharset()));
                                            }
                                        });
                            }
                        })
                .bind(8080);
    }
}
