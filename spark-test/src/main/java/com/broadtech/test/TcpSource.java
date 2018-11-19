package com.broadtech.test;

import com.broadtech.common.util.ThreadAssistant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class TcpSource {

    public static void main(String[] args) {
        ConcurrentLinkedQueue<ChannelHandlerContext> channels = new ConcurrentLinkedQueue<>();
        NioEventLoopGroup boos = new NioEventLoopGroup(1);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boos)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_SNDBUF, 4086)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                super.channelActive(ctx);
                                channels.offer(ctx);
                                System.out.println("remote connected : " + ctx.channel().remoteAddress());
                            }
                        });
                    }
                });
        serverBootstrap.bind(999);
        while (channels.isEmpty()) {
            ThreadAssistant.sleep(100, TimeUnit.MILLISECONDS);
        }
        System.out.println("starting send data ...");
        ChannelHandlerContext ctx = channels.poll();
        try {
            for (int i = 0; i < 300; i++) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(("This is seq " + i + "\n").getBytes()));
                ThreadAssistant.sleep(1, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("boos.shutdownGracefully()");
            boos.shutdownGracefully();
        }
    }
}
