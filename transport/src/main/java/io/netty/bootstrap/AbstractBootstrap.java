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

package io.netty.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link AbstractBootstrap}是一个帮助类，它使引导{@link Channel}变得很容易。它支持方法链接，提供了一种配置{@link AbstractBootstrap}的简单方法
 */
public abstract class AbstractBootstrap<B extends AbstractBootstrap<B, C>, C extends Channel> implements Cloneable {
    @SuppressWarnings("unchecked")
    static final Map.Entry<ChannelOption<?>, Object>[] EMPTY_OPTION_ARRAY = new Map.Entry[0];
    @SuppressWarnings("unchecked")
    static final Map.Entry<AttributeKey<?>, Object>[] EMPTY_ATTRIBUTE_ARRAY = new Map.Entry[0];

    volatile EventLoopGroup group;      // 事件循环组  NioEventLoopGroup
    @SuppressWarnings("deprecation")
    private volatile ChannelFactory<? extends C> channelFactory;    // ReflectiveChannelFactory(NioServerSocketChannel.class)
    private volatile SocketAddress localAddress;
    private final Map<ChannelOption<?>, Object> options = new ConcurrentHashMap<ChannelOption<?>, Object>();    // 管道操作
    private final Map<AttributeKey<?>, Object> attrs = new ConcurrentHashMap<AttributeKey<?>, Object>();
    private volatile ChannelHandler handler;    // 管道处理器  ChannelInitializer

    AbstractBootstrap() {
        // Disallow extending from a different package.
    }

    AbstractBootstrap(AbstractBootstrap<B, C> bootstrap) {
        group = bootstrap.group;
        channelFactory = bootstrap.channelFactory;
        handler = bootstrap.handler;
        localAddress = bootstrap.localAddress;
        options.putAll(bootstrap.options);
        attrs.putAll(bootstrap.attrs);
    }

    /**
     * The {@link EventLoopGroup} which is used to handle all the events for the to-be-created
     * {@link Channel}
     */
    public B group(EventLoopGroup group) {
        ObjectUtil.checkNotNull(group, "group");
        if (this.group != null) {
            throw new IllegalStateException("group set already");
        }
        this.group = group;
        return self();
    }

    @SuppressWarnings("unchecked")
    private B self() {
        return (B) this;
    }

    /**
     * The {@link Class} which is used to create {@link Channel} instances from.
     * You either use this or {@link #channelFactory(io.netty.channel.ChannelFactory)} if your
     * {@link Channel} implementation has no no-args constructor.
     */
    public B channel(Class<? extends C> channelClass) {
        return channelFactory(new ReflectiveChannelFactory<C>(
                ObjectUtil.checkNotNull(channelClass, "channelClass")
        ));
    }

    @Deprecated
    public B channelFactory(ChannelFactory<? extends C> channelFactory) {
        ObjectUtil.checkNotNull(channelFactory, "channelFactory");
        if (this.channelFactory != null) {
            throw new IllegalStateException("channelFactory set already");
        }

        this.channelFactory = channelFactory;
        return self();
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    public B channelFactory(io.netty.channel.ChannelFactory<? extends C> channelFactory) {
        return channelFactory((ChannelFactory<C>) channelFactory);
    }

    /**
     * The {@link SocketAddress} which is used to bind the local "end" to.
     */
    public B localAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
        return self();
    }

    /**
     * @see #localAddress(SocketAddress)
     */
    public B localAddress(int inetPort) {
        return localAddress(new InetSocketAddress(inetPort));
    }

    /**
     * @see #localAddress(SocketAddress)
     */
    public B localAddress(String inetHost, int inetPort) {
        return localAddress(SocketUtils.socketAddress(inetHost, inetPort));
    }

    /**
     * @see #localAddress(SocketAddress)
     */
    public B localAddress(InetAddress inetHost, int inetPort) {
        return localAddress(new InetSocketAddress(inetHost, inetPort));
    }

    /**
     * Allow to specify a {@link ChannelOption} which is used for the {@link Channel} instances once they got
     * created. Use a value of {@code null} to remove a previous set {@link ChannelOption}.
     */
    public <T> B option(ChannelOption<T> option, T value) {
        ObjectUtil.checkNotNull(option, "option");
        if (value == null) {
            options.remove(option);
        } else {
            options.put(option, value);
        }
        return self();
    }

    /**
     * Allow to specify an initial attribute of the newly created {@link Channel}.  If the {@code value} is
     * {@code null}, the attribute of the specified {@code key} is removed.
     */
    public <T> B attr(AttributeKey<T> key, T value) {
        ObjectUtil.checkNotNull(key, "key");
        if (value == null) {
            attrs.remove(key);
        } else {
            attrs.put(key, value);
        }
        return self();
    }

    /**
     * Validate all the parameters. Sub-classes may override this, but should
     * call the super method in that case.
     */
    public B validate() {
        if (group == null) {
            throw new IllegalStateException("group not set");
        }
        if (channelFactory == null) {
            throw new IllegalStateException("channel or channelFactory not set");
        }
        return self();
    }

    /**
     * Returns a deep clone of this bootstrap which has the identical configuration.  This method is useful when making
     * multiple {@link Channel}s with similar settings.  Please note that this method does not clone the
     * {@link EventLoopGroup} deeply but shallowly, making the group a shared resource.
     */
    @Override
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public abstract B clone();

    /**
     * Create a new {@link Channel} and register it with an {@link EventLoop}.
     */
    public ChannelFuture register() {
        validate();
        return initAndRegister();
    }

    /**
     * Create a new {@link Channel} and bind it.
     */
    public ChannelFuture bind() {
        validate();
        SocketAddress localAddress = this.localAddress;
        if (localAddress == null) {
            throw new IllegalStateException("localAddress not set");
        }
        return doBind(localAddress);
    }

    /**
     * Create a new {@link Channel} and bind it.
     */
    public ChannelFuture bind(int inetPort) {
        return bind(new InetSocketAddress(inetPort));
    }

    public ChannelFuture bind(String inetHost, int inetPort) {
        return bind(SocketUtils.socketAddress(inetHost, inetPort));
    }

    public ChannelFuture bind(InetAddress inetHost, int inetPort) {
        return bind(new InetSocketAddress(inetHost, inetPort));
    }

    public ChannelFuture bind(SocketAddress localAddress) {
        validate(); // bind
        return doBind(ObjectUtil.checkNotNull(localAddress, "localAddress"));
    }

    /****************服务启动****************/
    private ChannelFuture doBind(final SocketAddress localAddress) {
        final ChannelFuture regFuture = initAndRegister();  // NioEventLoopGroup.register() 得到 ChannelFuture
        final Channel channel = regFuture.channel();
        if (regFuture.cause() != null) {
            return regFuture;
        }

        if (regFuture.isDone()) {   // 如果此任务完成，则返回{@code true}
            ChannelPromise promise = channel.newPromise();
            doBind0(regFuture, channel, localAddress, promise); //
            return promise;
        } else {
            // 注册的未来几乎总是已经完成了，但是以防万一
            final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
            regFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    Throwable cause = future.cause();
                    if (cause != null) {
                        // EventLoop上的注册失败了，所以当我们试图访问通道的EventLoop时，不能让ChannelPromise直接导致IllegalStateException
                        promise.setFailure(cause);
                    } else {
                        // 注册成功，因此请设置要使用的正确执行程序
                        promise.registered();

                        doBind0(regFuture, channel, localAddress, promise);  // do
                    }
                }
            });
            return promise;
        }
    }

    /**
     * 初始化 NioServerSocketChannel
     * 1、通过 ReflectiveChannelFactory 通道工厂创建通道 Channel
     * 2、初始化(通道) 。由子类去实现初始化过程
     * <p>
     * 3、通过NioEventLoopGroup 注册通道 Channel 得到 ChannelFuture 并返回
     */
    final ChannelFuture initAndRegister() {
        Channel channel = null;
        try {
            channel = channelFactory.newChannel();  // ReflectiveChannelFactory -> NioServerSocketChannel
            init(channel);  //  抽象方法,  ServerBootstrap 添加使用 DefaultChannelPipeline.addLast(ChannelInitializer){initChannel()}
        } catch (Throwable t) {
            if (channel != null) {
                channel.unsafe().closeForcibly();
                return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
            }
            return new DefaultChannelPromise(new FailedChannel(), GlobalEventExecutor.INSTANCE).setFailure(t);
        }

        // DefaultChannelPromise
        ChannelFuture regFuture = config().group().register(channel);   // 利用线程池注册管道, ServerBootstrap -> NioEventLoopGroup
        if (regFuture.cause() != null) {
            if (channel.isRegistered()) {
                channel.close();
            } else {
                channel.unsafe().closeForcibly();
            }
        }

        return regFuture;
    }


    /**
     * 抽象方法， 子类实现初始化管道
     */
    abstract void init(Channel channel) throws Exception;


    /**
     * 最后实现方法， 通过线程池执行线程
     */
    private static void doBind0(final ChannelFuture regFuture, final Channel channel,
            final SocketAddress localAddress, final ChannelPromise promise) {

        // 在触发channelRegistered()之前调用此方法。让用户处理程序有机会在其channelRegistered()实现中设置管道
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                if (regFuture.isSuccess()) {
                    channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    promise.setFailure(regFuture.cause());
                }
            }
        });
    }

    /**
     * the {@link ChannelHandler} to use for serving the requests.
     */
    public B handler(ChannelHandler handler) {
        this.handler = ObjectUtil.checkNotNull(handler, "handler");
        return self();
    }

    /**
     * Returns the configured {@link EventLoopGroup} or {@code null} if non is configured yet.
     *
     * @deprecated Use {@link #config()} instead.
     */
    @Deprecated
    public final EventLoopGroup group() {
        return group;
    }

    /**
     * Returns the {@link AbstractBootstrapConfig} object that can be used to obtain the current config
     * of the bootstrap.
     */
    public abstract AbstractBootstrapConfig<B, C> config();

    final Map<ChannelOption<?>, Object> options0() {
        return options;
    }

    final Map<AttributeKey<?>, Object> attrs0() {
        return attrs;
    }

    final SocketAddress localAddress() {
        return localAddress;
    }

    @SuppressWarnings("deprecation")
    final ChannelFactory<? extends C> channelFactory() {
        return channelFactory;
    }

    final ChannelHandler handler() {
        return handler;
    }

    final Map<ChannelOption<?>, Object> options() {
        return copiedMap(options);
    }

    final Map<AttributeKey<?>, Object> attrs() {
        return copiedMap(attrs);
    }

    static <K, V> Map<K, V> copiedMap(Map<K, V> map) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<K, V>(map));
    }

    static void setAttributes(Channel channel, Map.Entry<AttributeKey<?>, Object>[] attrs) {
        for (Map.Entry<AttributeKey<?>, Object> e : attrs) {
            @SuppressWarnings("unchecked")
            AttributeKey<Object> key = (AttributeKey<Object>) e.getKey();
            channel.attr(key).set(e.getValue());
        }
    }

    static void setChannelOptions(
            Channel channel, Map.Entry<ChannelOption<?>, Object>[] options, InternalLogger logger) {
        for (Map.Entry<ChannelOption<?>, Object> e : options) {
            setChannelOption(channel, e.getKey(), e.getValue(), logger);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setChannelOption(
            Channel channel, ChannelOption<?> option, Object value, InternalLogger logger) {
        try {
            if (!channel.config().setOption((ChannelOption<Object>) option, value)) {
                logger.warn("Unknown channel option '{}' for channel '{}'", option, channel);
            }
        } catch (Throwable t) {
            logger.warn(
                    "Failed to set channel option '{}' with value '{}' for channel '{}'", option, value, channel, t);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder()
                .append(StringUtil.simpleClassName(this))
                .append('(').append(config()).append(')');
        return buf.toString();
    }

    /**
     * 实现 ChannelFuture 接口
     */
    static final class PendingRegistrationPromise extends DefaultChannelPromise {

        // Is set to the correct EventExecutor once the registration was successful. Otherwise it will
        // stay null and so the GlobalEventExecutor.INSTANCE will be used for notifications.
        private volatile boolean registered;

        PendingRegistrationPromise(Channel channel) {
            super(channel);
        }

        void registered() {
            registered = true;
        }

        @Override
        protected EventExecutor executor() {
            if (registered) {
                // If the registration was a success executor is set.
                //
                // See https://github.com/netty/netty/issues/2586
                return super.executor();
            }
            // The registration failed so we can only use the GlobalEventExecutor as last resort to notify.
            return GlobalEventExecutor.INSTANCE;
        }
    }
}
