package com.arminzheng.infrastructure.rpc.client;

import com.arminzheng.infrastructure.rpc.handler.RpcResponseMessageHandler;
import com.arminzheng.infrastructure.rpc.message.RpcRequestMessage;
import com.arminzheng.infrastructure.utility.AutoIncrement;
import io.netty.util.concurrent.DefaultPromise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 * RpcHandler
 *
 * @author zy
 * @since 2022.06.14
 */
@Component
@RequiredArgsConstructor
public class ServiceHandler {

    private final ClientLaunch clientLaunch;

    /**
     * 代理service
     *
     * @param serviceClass class
     * @return class instance
     * @param <T> class
     */
    public <T> T getProxyService(Class<T> serviceClass) {
        // 类加载器， 代理的实现接口的数组，
        ClassLoader loader = serviceClass.getClassLoader();
        Class<?>[] interfaces = {serviceClass};
        // serviceClass.getInterfaces(); // 获取的是实现的接口，而不是本接口，不能替换
        // 序号
        final int nextId = AutoIncrement.nextId();
        final Object proxyInstance =
                Proxy.newProxyInstance(
                        loader,
                        interfaces,
                        (proxy, method, args) -> {
                            final RpcRequestMessage msg =
                                    new RpcRequestMessage(
                                            nextId,
                                            serviceClass.getName(),
                                            method.getName(),
                                            method.getReturnType(),
                                            method.getParameterTypes(),
                                            args);
                            clientLaunch.channel().writeAndFlush(msg);
                            // 接收结果
                            DefaultPromise<Object> promise =
                                    new DefaultPromise<>(clientLaunch.channel().eventLoop());
                            RpcResponseMessageHandler.PROMISES.put(nextId, promise);

                            // 同步阻塞 invoke 方法本身会 throws Throwable 所以 await()不会抛异常
                            promise.await();
                            if (promise.isSuccess()) {
                                return promise.getNow(); // 调用正常
                            } else {
                                throw new RuntimeException(promise.cause()); // 调用失败
                            }
                        });
        return serviceClass.cast(proxyInstance);
    }
}
