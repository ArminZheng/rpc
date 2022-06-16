package com.arminzheng.infrastructure.utility;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求序号: 为了双工通信，提供异步能力
 *
 * @author zy
 */
public abstract class AutoIncrement {

    private static final AtomicInteger ID = new AtomicInteger();

    /**
     * Increment and Get 递增&获取
     *
     * @return 自增序号
     */
    public static int nextId() {
        return ID.incrementAndGet();
    }
}
