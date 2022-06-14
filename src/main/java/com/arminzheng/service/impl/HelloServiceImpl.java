package com.arminzheng.service.impl;

import com.arminzheng.service.HelloService;

/**
 * Hello 实现
 *
 * @author zy
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String msg) {
        // need to solve io.netty.handler.codec.TooLongFrameException: Adjusted frame length exceeds
        // 1024: 11509 - discarded
        int i = 1 / 0;
        return "server say: Hello! " + msg;
    }
}
