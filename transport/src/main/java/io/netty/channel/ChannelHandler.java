
package io.netty.channel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 处理I/O事件或拦截I/O操作，并将其转发给其{@link ChannelPipeline}中的下一个处理程序
 */
public interface ChannelHandler {

    /**
     * 将{@link ChannelHandler}添加到实际上下文并准备处理事件后调用
     */
    void handlerAdded(ChannelHandlerContext ctx) throws Exception;

    void handlerRemoved(ChannelHandlerContext ctx) throws Exception;

    // Caught 捕捉
    @Deprecated
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

    /**
     * Indicates that the same instance of the annotated {@link ChannelHandler}
     * can be added to one or more {@link ChannelPipeline}s multiple times
     * without a race condition.
     */
    @Inherited
    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sharable {
        // no value
    }
}
