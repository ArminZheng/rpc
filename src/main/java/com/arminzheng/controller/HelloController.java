package com.arminzheng.controller;

import com.arminzheng.infrastructure.rpc.client.ServiceHandler;
import com.arminzheng.service.HelloService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * HelloController
 *
 * @author zy
 * @since 2022.06.14
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloController {

    private final ServiceHandler serviceHandler;
    private HelloService helloService;

    @GetMapping("hello/{msg}")
    public String hello(@PathVariable("msg") String msg) {
        String result = helloService.sayHello(msg);
        log.error("rpc return msg: {}", result);
        return result;
    }

    @PostConstruct
    public void init() {
        helloService = serviceHandler.getProxyService(HelloService.class);
    }
}
