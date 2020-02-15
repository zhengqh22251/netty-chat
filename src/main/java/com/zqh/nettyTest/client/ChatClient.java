package com.zqh.nettyTest.client;

import com.zqh.nettyTest.client.handler.ChatClientHandler;
import com.zqh.nettyTest.protocol.IMDecoder;
import com.zqh.nettyTest.protocol.IMEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author：zhengqh
 * @date 2020/2/15 21:37
 **/
public class ChatClient {
     private ChatClientHandler chatClientHandler;
     private String host;
     private int port;

     public ChatClient(String name){
         this.chatClientHandler = new ChatClientHandler(name);
     }

     public void connect(String host,int port){
         this.host = host;
         this.port = port;
         EventLoopGroup workGroup = new NioEventLoopGroup();
         Bootstrap b = new Bootstrap();
         b.group(workGroup);
         b.channel(NioSocketChannel.class);
         b.option(ChannelOption.SO_KEEPALIVE,true);
         b.handler(new ChannelInitializer<SocketChannel>() {
             @Override
             protected void initChannel(SocketChannel channel) throws Exception {
                 ChannelPipeline pipeline= channel.pipeline();
                 pipeline.addLast(new IMDecoder());
                 pipeline.addLast(new IMEncoder());
                 pipeline.addLast(chatClientHandler);
             }
         });

         try {
             ChannelFuture f = b.connect(this.host,this.port).sync();
             f.channel().closeFuture().sync();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }finally {
             workGroup.shutdownGracefully();
         }
     }

    public static void main(String[] args) {
        new ChatClient("zhengqh").connect("localhost",8080);
    }
}
