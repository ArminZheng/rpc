package com.arminzheng.infrastructure.rpc.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * rpc 响应载体
 *
 * @author zy
 */
@Getter
@Setter
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {
    /** 返回值 */
    private Object returnValue;
    /** 异常值 */
    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}
