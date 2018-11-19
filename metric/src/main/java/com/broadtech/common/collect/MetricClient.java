package com.broadtech.common.collect;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Netty客服端提供向中心服务发送数据功能
 */
class MetricClient {
    private static final Logger logger = LoggerFactory.getLogger(MetricClient.class);

    /**
     * 发送协议: LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4)
     */
    void send(String server, int port, byte[] data) {
        EventLoopGroup loopGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        try {
            initClient(loopGroup, bootstrap);
            ChannelFuture future = connect(bootstrap, server, port);
            if (future.isSuccess()) {
                Channel channel = future.channel();
                channel.writeAndFlush(packet(data));
                channel.disconnect();
                logger.info("往" + server + ":" + port + "发送" + data.length + "B数据完成.");
            } else {
                logger.error(server + ":" + port + "连接失败，数据未能发送");
            }
        } catch (Exception e) {
            logger.error(e.toString(), e);
        } finally {
            disconnect(loopGroup);
        }
    }

    private ByteBuf packet(byte[] data) {
        ByteBuf byteBuf = Unpooled.buffer(data.length + 4);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
        return byteBuf;
    }

    private void initClient(EventLoopGroup bossGroup, Bootstrap bootstrap) {
        bootstrap.group(bossGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_LINGER, -1)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_RCVBUF, 2 * 1024 * 1024)
                .handler(new LoggingHandler(LogLevel.INFO));
    }

    private ChannelFuture connect(Bootstrap bootstrap, String server, int port) throws InterruptedException, UnknownHostException {
        InetSocketAddress remoteSocketAddress = new InetSocketAddress(InetAddress.getByName(server), port);
        logger.info("正在连接" + remoteSocketAddress.toString() + " ...");
        ChannelFuture future = bootstrap.connect(remoteSocketAddress).sync();
        logger.info("连接服务器: " + remoteSocketAddress.toString()
                + " status.success: " + future.isSuccess()
                + " status.done: " + future.isDone());
        return future;
    }

    private void disconnect(EventLoopGroup bossGroup) {
        bossGroup.shutdownGracefully();
    }
}
