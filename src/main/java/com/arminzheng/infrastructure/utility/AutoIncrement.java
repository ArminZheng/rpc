package com.arminzheng.infrastructure.utility;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * auto_increment
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
