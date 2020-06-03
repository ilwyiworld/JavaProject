package com.yiworld.client.init;

import com.yiworld.client.handle.ClientHandle;
import com.yiworld.common.protocol.ResponseProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientHandleInitializer extends ChannelInitializer<Channel> {

    private final ClientHandle cimClientHandle = new ClientHandle();

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                //10 秒没发送消息 将IdleStateHandler 添加到 ChannelPipeline 中
                .addLast(new IdleStateHandler(0, 10, 0))
                //心跳解码
                //.addLast(new HeartbeatEncode())
                // google Protobuf 编解码
                //拆包解码
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(ResponseProto.ResProtocol.getDefaultInstance()))
                //拆包编码
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder())
                .addLast(cimClientHandle)
        ;
    }
}
