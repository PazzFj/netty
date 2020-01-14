/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeMap;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * A nexus to a network socket or a component which is capable of I/O
 * operations such as read, write, connect, and bind.
 */
public interface Channel extends AttributeMap, ChannelOutboundInvoker, Comparable<Channel> {

    // 通道id
    ChannelId id();

    // 线程池
    EventLoop eventLoop();

    Channel parent();

    // 通道配置
    ChannelConfig config();

    // 是否打开
    boolean isOpen();

    // 是否注册
    boolean isRegistered();

    // 是否有效
    boolean isActive();

    ChannelMetadata metadata();

    SocketAddress localAddress();

    SocketAddress remoteAddress();

    ChannelFuture closeFuture();

    boolean isWritable();

    long bytesBeforeUnwritable();

    long bytesBeforeWritable();

    Unsafe unsafe();

    ChannelPipeline pipeline();

    ByteBufAllocator alloc();

    @Override
    Channel read();

    @Override
    Channel flush();

    interface Unsafe {

        RecvByteBufAllocator.Handle recvBufAllocHandle();

        SocketAddress localAddress();

        SocketAddress remoteAddress();

        void register(EventLoop eventLoop, ChannelPromise promise);

        void bind(SocketAddress localAddress, ChannelPromise promise);

        void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);

        void disconnect(ChannelPromise promise);

        void close(ChannelPromise promise);

        void closeForcibly();

        void deregister(ChannelPromise promise);

        void beginRead();

        void write(Object msg, ChannelPromise promise);

        void flush();

        ChannelPromise voidPromise();

        ChannelOutboundBuffer outboundBuffer();
    }
}
