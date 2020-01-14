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

import io.netty.util.concurrent.EventExecutorGroup;

/**
 * 特殊的{@link EventExecutorGroup}，它允许注册{@link Channel}，以便在事件循环期间进行后续选择
 */
public interface EventLoopGroup extends EventExecutorGroup {
    @Override
    EventLoop next();

    // 注册 Channel
    ChannelFuture register(Channel channel);

    ChannelFuture register(ChannelPromise promise);

    @Deprecated
    ChannelFuture register(Channel channel, ChannelPromise promise);
}
