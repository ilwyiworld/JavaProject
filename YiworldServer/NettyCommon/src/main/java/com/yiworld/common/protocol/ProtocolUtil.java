package com.yiworld.common.protocol;

import com.google.protobuf.InvalidProtocolBufferException;

public class ProtocolUtil {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        RequestProto.ReqProtocol protocol = RequestProto.ReqProtocol.newBuilder()
                .setRequestId(123L)
                .setReqMsg("你好啊")
                .build();
        byte[] encode = encode(protocol);
        RequestProto.ReqProtocol parseFrom = decode(encode);

        System.out.println(protocol.toString());
        System.out.println(protocol.toString().equals(parseFrom.toString()));
    }

    /**
     * 编码
     *
     * @param protocol
     * @return
     */
    public static byte[] encode(RequestProto.ReqProtocol protocol) {
        return protocol.toByteArray();
    }

    /**
     * 解码
     *
     * @param bytes
     * @return
     * @throws InvalidProtocolBufferException
     */
    public static RequestProto.ReqProtocol decode(byte[] bytes) throws InvalidProtocolBufferException {
        return RequestProto.ReqProtocol.parseFrom(bytes);
    }
}
