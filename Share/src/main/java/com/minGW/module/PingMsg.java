package com.minGW.module;

/**
 * Created by Gracecoder on 2017/10/20.
 * 心跳检测的消息类型
 */
public class PingMsg extends BaseMsg {
    public PingMsg() {
        super();
        setType(MsgType.PING);
    }
}
