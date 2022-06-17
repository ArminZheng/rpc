package com.arminzheng.exercise.buffer.diversity;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.arminzheng.infrastructure.utility.ByteBufferUtil.debugAll;

/**
 * 通过分隔符解决 ByteBuffer 黏包半包
 *
 * <p>网络上多条数据发送给服务端，数据之间使用 '\n' 进行分隔，但这些数据在接收时，被进行了重新组合，例如原始数据有3条:
 *
 * <pre>
 * Hello,world\n
 * I'm John\n
 * How are you?\n</pre>
 *
 * <p>变成了下面的两个 ByteBuffer (黏包半包)
 *
 * <p>Hello,world\nI'm John\nHo w are you?\n
 *
 * <p>接下来将错乱的数据恢复成原始的按 \n 分隔的数据
 */
public class TestStickyHalf {

    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        //                     11            24
        source.put("Hello,world\nI'm John\nHo".getBytes());
        split(source); // 第一半包会留到下一次
        source.put("w are you?\nhaha!\n".getBytes());
        split(source);
    }

    /**
     * 见到 '\n' 打印一行，没见到 '\n'，就压缩给下一次打印
     *
     * @param source ByteBuffer
     */
    private static void split(ByteBuffer source) {
        // 读模式
        source.flip();
        for (int index = 0; index < source.limit(); index++) {
            byte b = source.get(index);
            if (b != '\n') continue; // 不见兔子不撒鹰

            System.out.println("换行符的坐标 = " + index);
            int position = source.position();
            System.out.println("position 位置 = " + position);
            int length = index + 1 - position; // head\n12345\n 截取当前位置到 position 的位置的距离 i.e., 12345
            System.out.println("当前位置到 position 的位置的距离 = " + length);
            // 分配完整消息的 新buffer
            ByteBuffer target = ByteBuffer.allocate(length);

            System.out.print("position=[");
            // 从 source 读 ，写入 target
            for (int j = 0; j < length; j++) {
                target.put(source.get()); // 每一次 get 时， position++
                System.out.print(source.position() + ",");
            }
            System.out.println("]"); // 读取 position 位置

            debugAll(target); // 打印; 只有见到\n才打印，没见到\n，就让 compact 压缩给下一次
        }
        source.compact(); // 未读完部分向前压缩
    }

    @Test
    public void onlyTest1() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // byte 赋值方式有  字符形式，十进制、十六进制
        buffer.put((byte) 'a');
        buffer.put((byte) 0); // 十进制 0 是空字符
        buffer.put((byte) ' '); // 空格字符 ' ' 是（空格）
        buffer.put((byte) '\n'); // 字符 '\n' 是 换行键
        buffer.put((byte) 0x62); // 十六进制 0x62 是 b
        buffer.put((byte) 63); // 十进制 63 是 ?
        debugAll(buffer);

        // 打印 byte 数组 Arrays.toString(byte[])
        String s = "Hello, world\n";
        byte[] bytes = s.getBytes();
        System.out.println(Arrays.toString(bytes)); // 输出 ascII码 数组
        System.out.println(new String(bytes)); // 转义成字符串（换行也给转义了）
    }
}
