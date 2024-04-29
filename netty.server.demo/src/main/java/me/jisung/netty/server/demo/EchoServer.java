package me.jisung.netty.server.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "EchoServer")
public class EchoServer {

    @Value("${netty.server.demo.port}")
    private int port;

    private final EchoServerHandler echoServerHandler;

    @PostConstruct
    public void startServer() {
        asynchronousServer();
    }

    public void asynchronousServer() {
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        ByteBuf buffer = Unpooled.copiedBuffer("message", CharsetUtil.UTF_8);
        try {
            serverBootstrap.group(loopGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NonNull SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(@NonNull ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush(buffer.duplicate())
                                            .addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });

            printServerStartLog("AsynchronousEchoServer started at port {}");

            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("InterruptedException: {}", e.getMessage());
        } finally {
            loopGroup.shutdownGracefully();
        }
    }

    public void synchronousServer() {
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            // 서버용 부트스트랩 설정
            serverBootstrap.group(loopGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NonNull SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(echoServerHandler);
                        }
                    });

            printServerStartLog("SynchronousEchoServer started at port {}");

            // server를 비동기식으로 바인딩한다. sync()를 호출해 바인딩이 완료될 때까지 대기.
            ChannelFuture future = serverBootstrap.bind().sync();

            // 채널의 CloseFuture를 얻고 서버가 종료될때까지 메인 스레드 블로킹
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("InterruptedException: Server start failed");
        } finally {
            loopGroup.shutdownGracefully();
        }
    }

    private void printServerStartLog(String message) {
        log.info("=================================================================================");
        log.info(message, port);
        log.info("=================================================================================");
    }
}
