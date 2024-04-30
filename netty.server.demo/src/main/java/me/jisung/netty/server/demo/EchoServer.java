package me.jisung.netty.server.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "EchoServer")
public class EchoServer {

    @Value("${netty.server.demo.port}")
    private int port;

    private final EchoServerHandler echoServerHandler;
    private final EchoServerChannelActiveHandler echoServerChannelActiveHandler;

    @PostConstruct
    public void startServer() {
        synchronousServer();
    }

    public void synchronousServer() {
        // 이벤트루프도 스레드에 1:1 대응하기 때문에 사용시 적절한 갯수를 설정해야한다. 아래는 Netty에서 정의한 기본값 설정부
        // private static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
        EventLoopGroup loopGroup = new NioEventLoopGroup(3);
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            // 서버용 부트스트랩 설정
            serverBootstrap.group(loopGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NonNull SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    echoServerChannelActiveHandler,
                                    echoServerHandler
                            );
                        }
                    });


            log.info("=================================================================================");
            log.info("SynchronousEchoServer started at port {}", port);
            log.info("=================================================================================");

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
}
