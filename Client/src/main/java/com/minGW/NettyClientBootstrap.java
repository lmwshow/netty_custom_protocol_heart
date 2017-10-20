package com.minGW;

import com.minGW.module.AskMsg;
import com.minGW.module.AskParams;
import com.minGW.module.Constants;
import com.minGW.module.LoginMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.TimeUnit;

/**
 * Created by Gracecoder on 2017/10/20.
 */
public class NettyClientBootstrap {
    private int port;
    private String host;
    private SocketChannel socketChannel;
    private Bootstrap bootstrap;
    private EventLoopGroup workGroup = new NioEventLoopGroup();


    private static final EventExecutorGroup group = new DefaultEventExecutorGroup(20);

    public NettyClientBootstrap(int port, String host) throws InterruptedException {
        this.port = port;
        this.host = host;
        start();
    }

    private void start() throws InterruptedException {

        try {


            bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(workGroup);
            bootstrap.remoteAddress(host, port);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new IdleStateHandler(0, 0, 15));    //客户端5s没有读写的话,就触发读写空闲事件
                    socketChannel.pipeline().addLast(new ObjectEncoder());
                    socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                    socketChannel.pipeline().addLast(new NettyClientHandler(NettyClientBootstrap.this));
                }
            });


            doconncet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //客户端断线重连机制，需要Handler中 channelInactive 触发时 即断线是，调用此函数
    protected void doconncet() {


        if (socketChannel != null && socketChannel.isActive())
            return;

        //这里用sync的话，同步阻塞中，连接失败会抛出异常,然后直接被截获，那样只能在catch中睡眠然后再次调用doconnect实现定时重连。
        //而不是通过future的监听器回调的方法，实现任务调度schedule
        //如果用await()的话，可以获得future，那样不需要再catch中重连

        //不阻塞，直接异步也行。但是需要连接成功后，再进行通信，重写channelActive 进行登录。然后再发送信息
        final ChannelFuture future = bootstrap.connect(host, port);
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    socketChannel = (SocketChannel) future.channel();
                    System.out.println("connect server  成功---------");
                } else {
                    System.out.println("Failed to connect to server, try connect after 10s");
                    channelFuture.channel().eventLoop().schedule(new Runnable() {
                        public void run() {
                            try {
                                doconncet();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 10, TimeUnit.SECONDS);

                }
            }
        });
    }


    public static void main(String[] args) throws InterruptedException {
        Constants.setClientId("001");
        NettyClientBootstrap bootstrap = new NettyClientBootstrap(9999, "localhost");


        while (true) {

            //阻塞，知道socketChannel已经建立，且处于活动状态后，再开始发送信息
            while (!(bootstrap.socketChannel!=null && bootstrap.socketChannel.isActive()))
                TimeUnit.SECONDS.sleep(1);

            AskMsg askMsg=new AskMsg();
            AskParams askParams=new AskParams();
            askParams.setAuth("authToken");
            askMsg.setParams(askParams);
            bootstrap.socketChannel.writeAndFlush(askMsg);

            TimeUnit.SECONDS.sleep(3);
        }
    }
}
