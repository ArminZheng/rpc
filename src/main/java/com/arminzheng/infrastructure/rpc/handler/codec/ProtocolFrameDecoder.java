package com.arminzheng.infrastructure.rpc.handler.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 帧解码器 Handler
 *
 * <p>LengthFieldBasedFrameDecoder 不能抽离
 *
 * <p>1. 两个 EventLoop 线程，使用同一个对象，会产生线程安全的共享资源
 *
 * <p>2. 抽离出来的 LengthFieldBasedFrameDecoder 对象 主要记录了 多次消息之间的状态 就是线程不安全的
 *
 * <p>e.g., 半包黏包的上一个信息，就不能在多个 EventLoop 下使用同一个 Handler
 *
 * @author zy
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    public ProtocolFrameDecoder(
            int maxFrameLength,
            int lengthFieldOffset,
            int lengthFieldLength,
            int lengthAdjustment,
            int initialBytesToStrip) {
        super(
                maxFrameLength,
                lengthFieldOffset,
                lengthFieldLength,
                lengthAdjustment,
                initialBytesToStrip);
    }
}
