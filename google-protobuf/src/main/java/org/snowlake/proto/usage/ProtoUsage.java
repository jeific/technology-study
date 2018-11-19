package org.snowlake.proto.usage;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.snowlake.proto.Message;

/**
 * protobuf 序列化|反序列化
 */
public class ProtoUsage {

    public static void main(String[] args) throws InvalidProtocolBufferException {
        Message.SearchRequest request = Message.SearchRequest.newBuilder()
                .setQuery("get")
                .setPageNumber(0)
                .setResultPerPage(20)
                .build();

        System.out.println("getSerializedSize: " + request.getSerializedSize() + "\n");
        System.out.println(request);

        // 序列化
        byte[] encode = request.toByteArray();
        // 反序列化
        Message.SearchRequest decode = Message.SearchRequest.parseFrom(encode);

        System.out.println(decode);

        // JSON化 依赖： protobuf-java-util
        String json = JsonFormat.printer().print(request);
        System.out.println(json);
        System.out.println("encode size: " + request.getSerializedSize() + " json size: " + json.length());
    }
}
