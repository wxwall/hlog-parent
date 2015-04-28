package com.asiainfo.hlog.client.handle;

import com.asiainfo.hlog.client.model.LogData;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

/**
 * 通过http服务传递日志数据</br>
 * Created by chenfeng on 2015/4/15.
 */
public class HttpTransmitter extends AbstractTransmitter {

    private String logHttpServer = "http://localhost:8080/hlogServer/api";

    private String logHost = "localhost";

    private int post = 8080;

    //用于发送信息
    private Channel httpChannel = null;

    public HttpTransmitter(){
        name = "http";
    }

    @Override
    public void doInitialize() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        //创建一个channel，待会用它来发起链接
        NioSocketChannel channel = new NioSocketChannel();
        //为这个channel添加一个初始化的handler，用于响应待会channel建立成功
        channel.pipeline().addFirst(new HttpRequestHandler());
        //注册这个channel
        group.register(channel);
        //调用connect方法
        channel.connect(new InetSocketAddress(logHost, 8080));
    }

    @Override
    public void stop() {

    }


    public void transition(List<LogData> dataList) {

        if (httpChannel==null || !httpChannel.isActive()){
            System.err.println("无法通过http发送日志数据,请确认["+logHttpServer
                    +"]服务地址是否通畅.");
            //TODO 将日志数据持久层本地.
            //TODO 考虑建立重连
            return;
        }
        try {
            String msg = messageConver.convert(dataList);

            if(msg==null || msg.trim().length()==0){
                return;
            }

            System.out.println(msg);

            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                    logHttpServer, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));

            // 构建http请求
            request.headers().set(HttpHeaders.Names.HOST, logHost);
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());

            httpChannel.pipeline().write(request);
            httpChannel.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *
     */
    class HttpRequestHandler implements ChannelInboundHandler {

        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        }

        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            // TODO Auto-generated method stub
            System.out.println("handlerRemoved");
        }

        public void channelRegistered(ChannelHandlerContext ctx)
                throws Exception {
            // TODO Auto-generated method stub
            System.out.println("channelRegistered");
        }

        public void channelUnregistered(ChannelHandlerContext ctx)
                throws Exception {
            // TODO Auto-generated method stub
            System.out.println("channelUnregistered");
        }

        // 当连接建立成功之后会调用这个方法初始化channel
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            //添加一个http协议的encoder与decoder
            ctx.channel().pipeline().addFirst(new HttpClientCodec());
            //得到通信通道
            httpChannel = ctx.channel();
        }

        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // TODO Auto-generated method stub
            System.out.println("disconnect " + System.currentTimeMillis() / 1000);
        }

        public void channelRead(ChannelHandlerContext ctx, Object msg)
                throws Exception {
            // TODO Auto-generated method stub
            System.out.println("read " + System.currentTimeMillis() / 1000);
        }

        public void channelReadComplete(ChannelHandlerContext ctx)
                throws Exception {
            // TODO Auto-generated method stub
            System.out.println("channelReadComplete");
        }

        public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
                throws Exception {
            // TODO Auto-generated method stub
            System.out.println("userEventTriggered");
        }

        public void channelWritabilityChanged(ChannelHandlerContext ctx)
                throws Exception {
            // TODO Auto-generated method stub
            System.out.println("channelWritabilityChanged");
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                throws Exception {
            // TODO Auto-generated method stub
            System.out.println("error " + System.currentTimeMillis() / 1000);

        }

    }
}
