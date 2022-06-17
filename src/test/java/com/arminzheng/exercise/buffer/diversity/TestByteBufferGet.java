package com.arminzheng.exercise.buffer.diversity;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * java nio 的 ByteBuffer 的使用
 *
 * <p>1. allocate 分配内存
 *
 * <p>2. put 存放几个字符
 *
 * <p>3. flip 切换读模式
 *
 * <p>4. get 获取两个字节
 *
 * <p>5. rewind 下标 position 设 0 , 从头开始
 *
 * <p>6. mark 一下，给position做标记
 *
 * <p>7. get() 两次
 *
 * <p>8. reset 将 position 重置到 mark 位置
 *
 * @author zy
 */
public class TestByteBufferGet {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[] {'a', 'b', 'c', 'd'});
        buffer.flip(); // 3. 切换至读模式: position指针指向开头， limit指向写入的最后位置 (内存长度)
        // 4. get 获取两个字节
        buffer.get(new byte[2]);
        debugAll(buffer);
        // 5. rewind 下标position 设为0 ，从头开始
        buffer.rewind();
        debugAll(buffer);
        System.out.println(">>> get =" + (char) buffer.get());
        debugAll(buffer);
        // 6. mark 一下，给 position做标记  // : java.nio.HeapByteBuffer[pos=1 lim=4 cap=10]
        System.out.println(">>>  mark  得到的角标 = " + buffer.mark() + " ");
        // 再次 get() 两次
        System.out.println(">>> 第2个字节转为字符 = " + (char) buffer.get());
        System.out.println(">>> 第3个字节转为字符 = " + (char) buffer.get());
        debugAll(buffer);
        // 8. reset 将 position 重置到 mark 位置
        System.out.println(">>> reset " + buffer.reset());
        debugAll(buffer);
        System.out.println(">>> 第二个字节转为字符 = " + (char) buffer.get()); // b
        System.out.println(">>> 第三个字节转为字符 = " + (char) buffer.get()); // c
    }

    /**
     * 9. get(index) 指定下标获取
     *
     * <p>下标不会往后移动
     */
    @Test
    public void testGetSubscript() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[] {'a', 'b', 'c', 'd'});
        buffer.flip(); // 切换至读模式: position指针指向开头， limit指向写入的最后位置 (内存长度)
        System.out.println("第1个字节转为字符 =" + (char) buffer.get()); // a
        System.out.println("第2个字节转为字符 =" + (char) buffer.get()); // b
        System.out.println("指定下标读 =====" + (char) buffer.get(0)); // a
        System.out.println("指定下标读 =====" + (char) buffer.get(0)); // a
        debugAll(buffer);
    }
}
