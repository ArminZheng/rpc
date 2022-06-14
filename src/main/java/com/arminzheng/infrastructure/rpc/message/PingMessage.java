package com.arminzheng.infrastructure.rpc.message;

/**
 * 消息心跳
 *
 * @author zy
 */
public class PingMessage extends Message {
    @Override
    public int getMessageType() {
        return PingMessage;
    }
}
