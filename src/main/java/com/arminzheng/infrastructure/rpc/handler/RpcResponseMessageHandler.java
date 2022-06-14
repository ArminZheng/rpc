package com.arminzheng.infrastructure.rpc.handler;

import com.arminzheng.infrastructure.rpc.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务消费端
 *
 * <p>状态共享 + ConcurrentHashMap + remove() 单步操作 = @Sharable
 *
 * <p>使用 Sharable 必须没有线程安全问题
 *
 * @author zy
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) {
        final Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if (promise != null) { // 使用 promise 异步存放返回值
            final Object returnValue = msg.getReturnValue();
            final Exception exceptionValue = msg.getExceptionValue();
            if (exceptionValue != null) { // 约定
                promise.setFailure(exceptionValue);
            } else {
                promise.setSuccess(returnValue);
            }
        }
        log.debug("{}", msg);
    }
}
