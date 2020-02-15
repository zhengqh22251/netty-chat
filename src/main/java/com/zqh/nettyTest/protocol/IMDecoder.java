package com.zqh.nettyTest.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author：zhengqh
 * @date 2020/2/15 21:00
 **/

// 自定义IM协议的编码器
public class IMDecoder extends ByteToMessageDecoder {

    //解析IM写一下请求内容的正则
    private Pattern pattern = Pattern.compile("^\\[(.*)\\](\\s\\-\\s(.*))?");
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
       try {
           final int lenth = byteBuf.readableBytes();
           final byte[] array = new byte[lenth];

           String content = new String(array, byteBuf.readerIndex(), lenth);
           //空消息不解析
           if (!(content == null || "".equals(content.trim()))) {
               if (!IMP.isIMP(content)) {
                   // 责任链模式 这个解析器解析不了 往下？？
                   ctx.channel().pipeline().remove(this);
                   return;
               }
           }

           byteBuf.getBytes(byteBuf.readerIndex(), array, 0, lenth);
           list.add(new MessagePack().read(array, IMMessage.class));
           byteBuf.clear();
       }catch (Exception e){
           ctx.channel().pipeline().remove(this);
       }
    }


    /**
     * 字符串解析成自定义即时通信协议
     * @param msg
     * @return
     */
    public IMMessage decode(String msg){
        if(null == msg || "".equals(msg.trim())){ return null; }
        try{
            Matcher m = pattern.matcher(msg);
            String header = "";
            String content = "";
            if(m.matches()){
                header = m.group(1);
                content = m.group(3);
            }

            String [] heards = header.split("\\]\\[");
            long time = 0;
            try{ time = Long.parseLong(heards[1]); } catch(Exception e){}
            String nickName = heards[2];
            //昵称最多十个字
            nickName = nickName.length() < 10 ? nickName : nickName.substring(0, 9);

            if(msg.startsWith("[" + IMP.LOGIN.getName() + "]")){
                return new IMMessage(heards[0],heards[3],time,nickName);
            }else if(msg.startsWith("[" + IMP.CHAT.getName() + "]")){
                return new IMMessage(heards[0],time,nickName,content);
            }else if(msg.startsWith("[" + IMP.FLOWER.getName() + "]")){
                return new IMMessage(heards[0],heards[3],time,nickName);
            }else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
