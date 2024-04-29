package me.jisung.netty.server.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
@Slf4j(topic = "EchoServerHandler")
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * message가 들어올때 호출되는 method
     * @param ctx ChannelHandlerContext
     * @param msg Message Object
     * */
    @Override
    public void channelRead(
            @NonNull ChannelHandlerContext ctx,
            @NonNull Object msg
    ) {
        ByteBuf in = (ByteBuf) msg;
        log.info("message received: {}", in.toString(CharsetUtil.UTF_8));
        ctx.write(in);
    }

    /**
     * 모든 message가 처리되면 호출되는 method
     * @param ctx ChannelHandlerContext
     * */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // 대기 중 메시지를 원격 피어로 플러시하고 채널을 닫음
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * message 읽기 작업 중 예외 발생시 호출되는 method
     * @param ctx ChannelHandlerContext
     * @param cause Throwable
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exception caught", cause);
        ctx.close();
    }
}
