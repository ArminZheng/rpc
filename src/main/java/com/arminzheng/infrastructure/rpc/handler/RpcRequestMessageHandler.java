package com.arminzheng.infrastructure.rpc.handler;

import com.arminzheng.infrastructure.rpc.message.RpcRequestMessage;
import com.arminzheng.infrastructure.rpc.message.RpcResponseMessage;
import com.arminzheng.service.HelloService;
import com.arminzheng.service.ServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 服务调用端
 *
 * @author zy
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) {
        final RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(message.getSequenceId());
        try {
            Object invoke = invoke(message);
            response.setReturnValue(invoke);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            // 实际的错误 Caused by: java.lang.ArithmeticException: / by zero
            // 被包裹进了 InvocationTargetException，所以应该先 getCause 再获取信息 / by zero
            response.setExceptionValue(new Exception("远程调用出错：" + e.getCause().getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            response.setExceptionValue(new Exception("远程调用出错：" + e.getMessage()));
        }
        ctx.writeAndFlush(response);
    }

    public static Object invoke(RpcRequestMessage msg)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                    IllegalAccessException {
        // 获取接口类
        final Class<?> target = Class.forName(msg.getInterfaceName());
        // 获取实现类
        final HelloService service = (HelloService) ServiceFactory.getService(target);
        // 确定具体方法
        final Method method =
                service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
        // 执行方法
        return method.invoke(service, msg.getParameterValue());
    }
}
