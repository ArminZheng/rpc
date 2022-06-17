package com.arminzheng.exercise.buffer.manual;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * 字节缓冲区的父类Buffer中有几个核心属性
 *
 * <p>capacity：缓冲区的容量; 通过构造函数赋予，一旦设置，无法更改
 *
 * <p>limit：缓冲区的界限; 位于 limit 后的数据不可读写。缓冲区的限制不能为负，并且不能大于其容量
 *
 * <p>position：下一个读写位置的索引; 缓冲区的位置不能为负，并且不能大于limit
 *
 * <p>mark：记录当前position的值; position被改变后，可以通过调用reset() 方法恢复到mark的位置
 *
 * <p>以上四个属性必须满足以下要求 mark <= position <= limit <= capacity
 */
@Slf4j
public class TestByteBufferAllocate {

    public static void main(String[] args) {
        // java.nio.HeapByteBuffer   -java堆内存， 读写效率低， 受垃圾回收 GC的影响
        System.out.println(ByteBuffer.allocate(16).getClass());

        // java.nio.DirectByteBuffer -直接内存，读写效率高(少一次拷贝)，不受GC的影响；使用完后，需要彻底的释放以免内存泄露
        System.out.println(ByteBuffer.allocateDirect(16).getClass());

        ByteBuffer buffer = ByteBuffer.allocate(10);
        debugAll(buffer); // 1 初始

        buffer.put("hello".getBytes());
        debugAll(buffer); // 2 放完

        buffer.flip();
        debugAll(buffer); // 3 flip 翻转后

        log.info("buffer.get() = {}", (char) buffer.get());
        debugAll(buffer); // 4 get 之后内容不变，position 往后移

        buffer.compact(); // position: [4], limit: [10] 内容往前移，尾巴会有脏数据
        debugAll(buffer); // 5 compact 【直接get会出现脏数据】之后一般是写入 (所以无影响)
        // position 初始在 0 下标，compact 后位于即将写入的位置（该位置会有往前移产生的脏数据）
        // limit 会限制在末尾，初始在capacity，flip 后位于有效数据后一位

        // 假设此时故意get
        log.info("此时故意get = {}", (char) buffer.get());
        debugAll(buffer); // 5' get 之后内容不变，position 往后移, 脏数据就变成正常数据了

        buffer.flip();
        debugAll(buffer); // 6 flip 经过研究，limit 只是把 position 的值读取过来当成 limit 的值

        log.info("buffer.get() = {}", (char) buffer.get());
        debugAll(buffer); // 7 after get

        buffer.compact();
        debugAll(buffer); // 8 before put
        buffer.put("world".getBytes());
        debugAll(buffer); // 9 after put

        buffer.flip(); // 翻转，又名读模式
        log.info("buffer.get() = {}", (char) buffer.get());
        debugAll(buffer); // 10 get 正常获取
    }
}
