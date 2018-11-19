package com.broadtech.common.collect;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Netty客服端提供向中心服务发送数据功能
 */
class CollectSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(CollectSocketServer.class);
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(3);
    private final ConcurrentSkipListSet<ChannelHandlerContext> channels = new ConcurrentSkipListSet<>();

    private ByteBuf packet(byte[] data) {
        ByteBuf byteBuf = Unpooled.buffer(data.length + 4);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
        return byteBuf;
    }

    public void shutdownGracefully() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    private void initClient(int port) throws InterruptedException, IOException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_RCVBUF, 64 * 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                                new ChunkedWriteHandler(),
                                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4),
                                new InternalReceiveHandler());
                    }
                });
        ChannelFuture futrue = bootstrap.bind(port).sync();
        if (!futrue.isSuccess()) throw new IOException(port + " bind failure!");
    }

    private static class InternalReceiveHandler extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        }


    }
}
