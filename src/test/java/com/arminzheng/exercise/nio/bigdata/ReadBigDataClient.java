package com.arminzheng.exercise.nio.bigdata;

import com.arminzheng.exercise.nio.CommonClient;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 接收大量数据客户端
 *
 * <p>接收服务端的多次发送
 */
public class ReadBigDataClient extends CommonClient {

    public static void main(String[] args) throws IOException {
        int count = 0;
        while (true) { // 接收数据
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            int read = sc.read(buffer); // 没有消息会阻塞
            if (read == -1) {
                break;
            }
            count += read;
            System.out.println("目前总数 " + count);
            buffer.clear();
        }
    }
}
