package com.arminzheng.infrastructure.rpc.message;

/**
 * 消息心跳响应
 *
 * @author zy
 */
public class PongMessage extends Message {
    @Override
    public int getMessageType() {
        return PongMessage;
    }
}
