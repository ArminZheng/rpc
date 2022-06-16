package com.arminzheng.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * Server
 *
 * @author zy
 * @version 2022/6/3
 */
@Slf4j
public class HttpServer {

    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup(2); // 可以指定核心数
        log.info("默认 Group数为 {}", NettyRuntime.availableProcessors() * 2);
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.channel(NioServerSocketChannel.class);
            server.group(boss, worker);
            server.childHandler(new ChannelInitial());
            ChannelFuture future = server.bind(80).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    static class ChannelInitial extends ChannelInitializer<NioSocketChannel> {
        @Override
        protected void initChannel(NioSocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpRequestInbound());
            pipeline.addLast(new HttpContentInbound());
        }
    }

    private static class HttpRequestInbound extends SimpleChannelInboundHandler<HttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) {
            log.info("uri is {}", msg.uri());
            log.info("headers is {}", msg.headers());
            DefaultFullHttpResponse response =
                    new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
            byte[] bytes = "<h1>Hello world/~</h1>".getBytes();
            response.headers().setInt(CONTENT_LENGTH, bytes.length);
            response.content().writeBytes(bytes);
            ctx.writeAndFlush(response);
        }
    }

    private static class HttpContentInbound extends SimpleChannelInboundHandler<HttpContent> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpContent msg) {
            log.info("requestBody is {}", msg.toString());
        }
    }
}
