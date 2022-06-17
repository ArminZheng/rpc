package com.arminzheng.exercise.stickandhalf.stickandhalf_fixed_length;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * 定长解码器 解决黏包
 *
 * @author zy
 */
public class Client {
    static final Logger log = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        send();
        System.out.println("send......finish......");
    }

    private static void send() {
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline()
                                    .addLast(
                                            new ChannelInboundHandlerAdapter() {
                                                // channel连接建立好之后 出发 channelActive() 时间
                                                @Override
                                                public void channelActive(
                                                        ChannelHandlerContext ctx) {
                                                    log.debug("sending...");
                                                    final Random r = new Random();
                                                    final ByteBuf buf = ctx.alloc().buffer();
                                                    for (int i = 0; i < 10; i++) {
                                                        final int idx = r.nextInt(10) + i * 10;
                                                        buf.writeBytes(
                                                                new byte[] {
                                                                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10
                                                                });
                                                        buf.setByte(idx, 'a');
                                                    }

                                                    ctx.writeAndFlush(buf);
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
