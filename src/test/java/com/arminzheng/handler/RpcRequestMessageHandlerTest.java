package com.arminzheng.handler;

import com.arminzheng.infrastructure.rpc.handler.RpcRequestMessageHandler;
import com.arminzheng.infrastructure.rpc.message.RpcRequestMessage;

/**
 * 服务调用-测试
 *
 * @author zy
 * @since 2022.06.14
 */
class RpcRequestMessageHandlerTest {

    public static void main(String[] args) throws Throwable {
        final RpcRequestMessage requestMsg =
                new RpcRequestMessage(
                        1,
                        "com.arminzheng.service.HelloService",
                        "sayHello",
                        String.class,
                        new Class[] {String.class},
                        new Object[] {"wonderful world!"});
        final Object invoke = RpcRequestMessageHandler.invoke(requestMsg);
        System.out.println(invoke);
    }
}
