package com.arminzheng.exercise.netty.hello;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class HelloServer {

    public static void main(String[] args) {

        // 1. 服务端 的 启动器， 组装 协调 下面很多组件 进行启动
        new ServerBootstrap()
                /*
                2. 添加 事件循环组 NioEventLoopGroup 组件 (相当于 BossEventLoop 和 WorkerEventLoop 加在一起)
                    一个 NioEvenLoopGroup 包含 多个 NioEventLoop
                    NioEventLoop 简单理解：包含了线程池 和 选择器 selector 管理 accept read等事件
                    NioEventLoop 流程   : accept 事件进来交给 AEventLoop, read 事件进来交给 BEventLoop 接收到 ByteBuf
                                      然后 A B 都会交给处理器 childHandler 处理
                        BossEventLoop
                            作用：处理可连接事件 accept
                        WorkerEventLoop
                            作用：处理可读事件 read
                            解释：Loop 表示循环，循环处理事件(Event)
                            组件：包含 selector 进行监测 各种事件
                            包含 thread
                 */
                .group(new NioEventLoopGroup())
                /*
                3. 选择一个通用的 基于NIO  ServerSocketChannel 的实现 （netty支持好几种实现）
                     还支持 OioServerSocketChannel(old ) 就是 BIO 阻塞IO 的实现
                     还支持某种操作系统(linux)特别优化的实现 NIO / EPOLL / KQUEUE 等
                 */
                .channel(NioServerSocketChannel.class)
                /*
                 * 4. boss 负责处理连接的，worker(child) 负责读写的 他决定了worker(child) 能执行哪些操作(handler)
                 *      这里的 childHandler 添加的各种处理器 给 每个子线程的 SocketChannel用的
                 */
                .childHandler(
                        /*
                         * 5. channel 代表与客户端进行数据读写通道
                         *
                         * ChannelInitializer 触发处理器(SocketChannel连接后 才执行)，负责添加别的 handler;
                         */
                        new ChannelInitializer<NioSocketChannel>() {
                            // initChannel可以添加更多处理器
                            protected void initChannel(NioSocketChannel ch) {
                                /*
                                 *  6. 添加具体 handler ( ch.pipeline().addLast(添加一道道的工序 )
                                 *
                                 *      StringDecoder 把 传输来的 ByteBuf 转换成字符串
                                 */
                                ch.pipeline().addLast(new StringDecoder());

                                /*
                                 * 7. ChannelInboundHandlerAdapter 自定义的 handler
                                 * @param ctx
                                 * @param msg          上一步 ByteBuf 转换的字符串
                                 * @throws Exception
                                 */
                                ch.pipeline()
                                        .addLast(
                                                new ChannelInboundHandlerAdapter() {

                                                    @Override
                                                    public void channelRead(
                                                            ChannelHandlerContext ctx, Object msg) {
                                                        //
                                                        // super.channelRead(ctx, msg);
                                                        System.out.println(msg);
                                                    }
                                                });
                            }
                        })
                // 8. 绑定 监听端口
                .bind(8080);
    }
}
