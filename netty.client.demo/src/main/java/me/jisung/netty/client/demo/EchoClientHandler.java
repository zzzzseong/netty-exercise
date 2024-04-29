package me.jisung.netty.client.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ChannelHandler.Sharable
@Slf4j(topic = "EchoClientHandler")
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 서버에 대한 연결이 활성화될 때 호출되는 method
     * @param ctx ChannelHandlerContext
     * */
    @Override
    public void channelActive(@NonNull ChannelHandlerContext ctx) {
        UUID uuid = UUID.randomUUID();

        // 채널 활성화 시 메시지를 서버로 전송
        ctx.writeAndFlush(Unpooled.copiedBuffer("[" + uuid + "] Netty rocks!", CharsetUtil.UTF_8));
    }

    /**
     * 서버로부터 message 수신 시 호출되는 method
     * @param ctx ChannelHandlerContext
     * @param in ByteBuf
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        log.info("message received: {}", in.toString(CharsetUtil.UTF_8));
    }

    /**
     * 예외발생시 호출되는 method - logging and channel close
     * @param ctx ChannelHandlerContext
     * @param cause Throwable
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception caught", cause);
        ctx.close();
    }
}
