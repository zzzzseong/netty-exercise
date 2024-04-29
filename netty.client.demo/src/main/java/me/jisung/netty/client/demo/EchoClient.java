package me.jisung.netty.client.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j(topic = "EchoClient")
public class EchoClient {

    @Value("${netty.client.demo.host}")
    private String host;

    @Value("${netty.client.demo.port}")
    private int port;

    private static final int THREAD_POOL_SIZE = 100;

    @PostConstruct
    public void startClient() throws InterruptedException {

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(THREAD_POOL_SIZE);

        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            threadPool.execute(() -> {
                try {
                    start();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        threadPool.shutdown();
    }

    public void start() {
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        try {
            // 클라이언트용 부트스트랩 설정
            bootstrap.group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NonNull SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new EchoClientHandler());
                        }
                    });

            // channelFuture를 얻고 연결이 완료될 때까지 대기
            ChannelFuture future = bootstrap.connect().sync();

            // channel이 닫힐때까지 대기
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("InterruptedException: Client start failed", e);
        } finally {
            loopGroup.shutdownGracefully();
        }
    }
}
