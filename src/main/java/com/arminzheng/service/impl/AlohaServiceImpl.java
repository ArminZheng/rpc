package com.arminzheng.service.impl;

import com.arminzheng.service.HelloService;

/**
 * Aloha 实现
 *
 * @author zy
 */
public class AlohaServiceImpl implements HelloService {
    @Override
    public String sayHello(String msg) {
        return "server say: Aloha! " + msg;
    }
}
