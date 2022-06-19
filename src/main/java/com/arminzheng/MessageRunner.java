package com.arminzheng;

import com.arminzheng.infrastructure.rpc.client.ServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import java.util.Scanner;

/**
 * MessageRunner
 *
 * @author zy
 * @since 2022.06.14
 */
@SpringBootApplication
@RequiredArgsConstructor
public class MessageRunner {

    private final ServiceHandler handler;

    public static void main(String[] args) {
        SpringApplication.run(MessageRunner.class, args);
    }

    @Bean
    @DependsOn("clientLauncher")
    public ApplicationRunner runner() {
        return args ->
                new Thread(
                                () -> {
                                    for (; ; ) {
                                        Scanner scanner = new Scanner(System.in);
                                        String msg = scanner.nextLine();
                                        boolean sent = handler.send(msg);
                                        if (sent) {
                                            System.out.println("发送成功...");
                                        } else {
                                            for (int i = 0; i < 3; i++) {
                                                if (handler.send(msg)) {
                                                    System.out.println("发送成功...");
                                                    break;
                                                } else if (i == 2) {
                                                    System.out.println("发送失败!");
                                                }
                                            }
                                        }
                                    }
                                },
                                "message")
                        .start();
    }
}
