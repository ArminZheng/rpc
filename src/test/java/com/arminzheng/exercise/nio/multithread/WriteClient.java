package com.arminzheng.exercise.nio.multithread;

import com.arminzheng.exercise.nio.CommonClient;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 多线程 优化前面的案例
 */
public class WriteClient extends CommonClient {

    public static void main(String[] args) throws IOException {
        sc.write(Charset.defaultCharset().encode("0123456789abcdef"));
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
    }
}
