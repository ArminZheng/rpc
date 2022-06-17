package com.arminzheng.exercise.stickandhalf.phenomenon;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 黏包：
 *
 * <p>接收方 ByteBuf 设置的太大（Netty默认1024）
 *
 * <p>滑动窗口（TCP层面）: 发送一个消息，就必须等待一个消息应答
 *
 * <p>> 为了解决等待的问题，引入窗口的概念，即无需等待应答而可以继续发送的数据最大值
 *
 * <p>> 应答 n个 ack，就往前移动 n行，又可以发送 n条 数据，窗口不断往前移动
 *
 * <p>Nagle 算法: 会造成黏包（避免 40kb 的 tcp包只带有 1kb 数据）
 *
 * <p>半包：
 *
 * <p>接收方 ByteBuf 小于实际发送数据量
 *
 * <p>滑动窗口（TCP层面）: 接收方窗口只剩128bytes，而发送方报文是 256bytes，就必须等待 ack应答后才能发送剩余数据
 *
 * <p>MSS 限制（max segment size，传输层）: 超过MSS限制会将数据切分发送，就会造成半包 (约等于 MTU - 20 ip头 - 20 tcp头)
 *
 * <p>MTU（max transfer unit，链路层，一般在1500）
 *
 * <pre>
 * |Ethernet Header 14bytes IP Header 20bytes TCP Header 20bytes Payload 1460bytes FCS 4bytes|
 * |-----------------------|------------------------ IP MTU ----------------------|----------|
 * |-----------------------|--------------------- Ethernet MTU -------------------|----------|
 * |------------------------------------------------------------|-------TCP MSS---|----------|</pre>
 */
@Slf4j
public class HelloWorldClient {

    public static void main(String[] args) {
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(
                                            new ChannelInboundHandlerAdapter() {
                                                // channel连接建立好之后 出发 channelActive() 时间
                                                @Override
                                                public void channelActive(ChannelHandlerContext ctx) {
                                                    // super.channelActive(ctx);
                                                    for (int i = 0; i < 10; i++) {
                                                        final ByteBuf buf = ctx.alloc().buffer(16);
                                                        buf.writeBytes(
                                                                new byte[] {
                                                                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                                                    10, 11, 12, 13, 14, 15
                                                                });
                                                        ctx.writeAndFlush(buf);
                                                    }
                                                }
                                            });
                        }
                    });
            final ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }
}
