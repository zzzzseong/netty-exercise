# Netty

<img src="./images/netty-description.png" width=1000>

[Netty user guide for 4.x](https://netty.io/index.html)

Netty는 `비동기 네트워크 프레임워크`이다. 
클라이언트는 각각의 `소켓채널`을 이용해 `서버소켓채널`에 연결하고 서버에서는 미리 그룹으로 정의한 `이벤트 루프 스레드`들에 각각의 클라이언트를 배정해 요청을 처리한다. 
같은 이벤트 루프 스레드와 연결된 채널 요청에 한해서 순서가 보장된다.   

## Netty Architecture
<img src="./images/netty-architecture.png" width="1000">

### ServerSide
<img src="./images/netty-architecture-server-side.png" width="1000">
ServerBootstrap에 EventLoop를 설정하고, 생성된 모든 SocketChannel을 임의의 EventLoop에 등록한다.
SocketChannel마다 Inbound 요청 처리를 위한 ChannelPipeline이 존재하고 
ChannelInboundHandlerAdapter를 상속받는 Handler 클래스를 구현해 pipeline에 chaining한다.
handler에서 발생한 event는 등록된 EventLoop에서 비동기처리한다.

### ClientSide
<img src="./images/netty-architecture-client-side.png" width="1000">

## Netty Server Flow
<img src="./images/netty-server-bind-flow.png" width="1000">

## Netty Client Execution Flow
<img src="./images/netty-client-connect-flow.png" width="1000">

## Netty EventLoop
![event-loop-mechanism](./images/event-loop-mechanism.gif)