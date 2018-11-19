package com.broadtech.test;

import com.broadtech.common.util.ThreadAssistant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

public class SimpleCllent {

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                super.channelActive(ctx);
                                System.out.println("== active ==" + ctx.channel().remoteAddress());
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buffer = (ByteBuf) msg;
                                byte[] data = new byte[buffer.readableBytes()];
                                buffer.readBytes(data);
                                System.out.print(new String(data));
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                super.channelInactive(ctx);
                                System.out.println("Runtime.getRuntime().halt(0)");
                                Runtime.getRuntime().halt(0);
                            }
                        });
                    }
                });
        bootstrap.connect("localhost", 999);
        while (bootstrap != null) {
            ThreadAssistant.sleep(1, TimeUnit.SECONDS);
        }
        bootstrap.group().shutdownGracefully();
    }
}
