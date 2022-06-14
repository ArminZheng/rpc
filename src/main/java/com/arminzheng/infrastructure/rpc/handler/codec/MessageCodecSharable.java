package com.arminzheng.infrastructure.rpc.handler.codec;

import com.arminzheng.infrastructure.config.SerializerConfiguration;
import com.arminzheng.infrastructure.rpc.message.Message;
import com.arminzheng.infrastructure.utility.SerializerEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息编解码 支持@Sharable
 *
 * <p>父类 MessageToMessageCodec 认为是完整的信息不用考虑半包黏包问题, 所以必须保证上一个处理器是帧解码器
 *
 * <pre>
 * <b>魔数</b>: 用来在第一时间判定是否是无效数据包
 * <b>版本号</b>: 可以支持协议的升级
 * <b>序列化算法</b>: 消息正文到底采用哪种序列化反序列化方式，可以由此扩展，例如：json、protobuf、hessian、jdk
 * <b>指令类型</b>: 是登录、注册、单聊、群聊... 跟业务相关
 * <b>请求序号</b>: 为了双工通信，提供异步能力
 * <b>正文长度</b>
 * <b>消息正文</b></pre>
 *
 * @author zy
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {

    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) {
        SerializerEnum.Algorithm algorithm = SerializerConfiguration.serializerSelector();
        ByteBuf out = ctx.alloc().buffer();
        out.writeBytes(new byte[] {1, 2, 3, 4}); // 4字节 魔数
        out.writeByte(1); // 1字节 版本号
        out.writeByte(algorithm.ordinal()); // 1字节 序列化方式 0-jdk,1-json
        log.info("encode msg.getMessageType() is {}", msg.getMessageType());
        out.writeByte(msg.getMessageType()); // 1字节 指令类型
        out.writeInt(msg.getSequenceId()); // 4字节 请求序号 【大端】
        out.writeByte(0xff); // 1字节 填充为2的整数倍
        final byte[] bytes = algorithm.serialize(msg);
        // 写入内容 长度
        out.writeInt(bytes.length);
        // 写入内容
        out.writeBytes(bytes);
        // 加入List 方便传递给 下一个Handler
        outList.add(out);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int magicNum = in.readInt(); // 大端4字节 魔数
        byte version = in.readByte(); // 版本
        byte serializerType = in.readByte(); // 0 Java 1 Json
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        final byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length); // 按长度读取

        // 反序列化算法
        final SerializerEnum.Algorithm algorithm =
                SerializerEnum.Algorithm.values()[serializerType];
        // 消息具体类型
        final Object message = algorithm.deserialize(Message.getMessageClass(messageType), bytes);
        log.debug(
                "{},{},{},{},{},{}",
                magicNum,
                version,
                serializerType,
                messageType,
                sequenceId,
                length);
        log.debug("{}", message);
        // 加入List 方便传递给 下一个Handler
        out.add(message);
    }
}
