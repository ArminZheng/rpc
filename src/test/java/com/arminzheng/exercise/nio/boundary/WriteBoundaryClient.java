package com.arminzheng.exercise.nio.boundary;

import com.arminzheng.exercise.nio.CommonClient;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 客户端
 *
 * @since 2022-05-29
 */
public class WriteBoundaryClient extends CommonClient {

    /** 与服务器约定好，这里 必须 每个消息 用 \n 结尾 */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static void main(String[] args) throws IOException {
        sc.write(Charset.defaultCharset().encode("0123456789abcdef3333\nworld\n"));
        // 阻塞方法，等待控制台输入
        System.in.read();
    }
}
