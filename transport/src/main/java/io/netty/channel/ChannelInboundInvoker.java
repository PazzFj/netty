/*
 * Copyright 2016 The Netty Project
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

/**
 * 管道 流入 调用
 */
public interface ChannelInboundInvoker {

    /**
     * Channel注册事件
     */
    ChannelInboundInvoker fireChannelRegistered();

    /**
     * A {@link Channel} was unregistered from its {@link EventLoop}.
     *
     * This will result in having the  {@link ChannelInboundHandler#channelUnregistered(ChannelHandlerContext)} method
     * called of the next  {@link ChannelInboundHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelUnregistered();

    /**
     * Tcp链路建立成功，Channel激活事件
     */
    ChannelInboundInvoker fireChannelActive();

    /**
     * Tcp连接关闭，链路不可用通知事件
     */
    ChannelInboundInvoker fireChannelInactive();

    /**
     * 异常通知事件
     */
    ChannelInboundInvoker fireExceptionCaught(Throwable cause);

    /**
     * 用户自定义事件
     */
    ChannelInboundInvoker fireUserEventTriggered(Object event);

    /**
     * 读事件
     */
    ChannelInboundInvoker fireChannelRead(Object msg);

    /**
     * 读操作完成通知事件
     */
    ChannelInboundInvoker fireChannelReadComplete();

    /**
     * Channel的可写状态变化通知事件
     */
    ChannelInboundInvoker fireChannelWritabilityChanged();
}
