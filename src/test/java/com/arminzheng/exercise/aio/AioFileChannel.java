package com.arminzheng.exercise.aio;

import com.arminzheng.infrastructure.utility.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

/**
 * 异步 IO 测试 (多线程 + 异步IO)
 *
 * <p>多线程才能实现 异步
 *
 * <p>这里系统自动创建的多线程是守护线程：当进程中没有任何非守护线程在运行，那么守护线程会直接终止
 */
@Slf4j
public class AioFileChannel {

    public static void main(String[] args) throws IOException {
        // open方法第三个参数为线程池 (异步io必须一个线程发起read 另一个线程送结果) 这里不用回传结果故没有设置
        try (AsynchronousFileChannel channel =
                AsynchronousFileChannel.open(Paths.get("data.txt"), StandardOpenOption.READ)) {
            final ByteBuffer buffer = ByteBuffer.allocate(16);
            /*
            参数1：ByteBuffer
            参数2：起始位置
            参数3：附件，一个读不完 需要另一个 byteBuffer
            参数4：包含两个回调方法 的 回调对象CompletionHandler ，回调 接收结果的操作
                  当然这两个回调方法 一定是 另一个线程操作的 */
            channel.read(
                    buffer,
                    0,
                    buffer,
                    new CompletionHandler<Integer, ByteBuffer>() {
                        /**
                         * (某一次) read成功的回调方法
                         *
                         * @param result 读到的实际字节数
                         * @param attachment 传过来的 buffer 对象
                         */
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            log.debug("read completed... 读取字节数 = {}", result);
                            attachment.flip();
                            ByteBufferUtil.debugAll(attachment);
                        }

                        // read 过程中出现了异常，将被调用
                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });
            log.info("read end.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " running...");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
