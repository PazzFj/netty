package io.netty.bootstrap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;

/**
 * @author: 彭坚
 * @create: 2020/2/11 12:52
 * @description:
 */
public class TestMain {

    public static void main(String[] args) throws Exception {

        SelectorProvider provider1 = SelectorProvider.provider();
        SelectorProvider provider2 = SelectorProvider.provider();
        SelectorProvider provider3 = SelectorProvider.provider();
        SelectorProvider provider4 = SelectorProvider.provider();
        SelectorProvider provider5 = SelectorProvider.provider();
        System.out.println(provider1.openSelector());
        System.out.println(provider2.openSelector());
        System.out.println(provider3.openServerSocketChannel());
        System.out.println(provider4.openServerSocketChannel().configureBlocking(false).register(Selector.open(),  SelectionKey.OP_ACCEPT));
        System.out.println(provider5);

        /*ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf bb = (ByteBuf) msg;
                                // 创建一个和buf同等长度的字节数组
                                byte[] reqByte = new byte[bb.readableBytes()];
                                // 将buf中的数据读取到数组中
                                bb.readBytes(reqByte);
                                String reqStr = new String(reqByte, Charset.forName("utf-8"));
                                System.err.println("server 接收到客户端的请求： " + reqStr);
                                String respStr = new StringBuilder("来自服务器的响应").append(reqStr).append("$_").toString();

                                // 返回给客户端响应                                                                                                                                                       和客户端链接中断即短连接，当信息返回给客户端后中断
                                ctx.writeAndFlush(Unpooled.copiedBuffer(respStr.getBytes()));
                            }
                        });
                    }
                })
                .bind(8899).sync();

        channelFuture.channel().closeFuture().sync();*/
    }

}
