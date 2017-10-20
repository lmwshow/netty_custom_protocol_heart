package com.minGW.module;

import java.io.Serializable;

/**
 * Created by Gracecoder on 2017/10/20.
 *
 * 必须实现序列，serialVersionUID 一定要有
 */
public abstract class BaseMsg implements Serializable {

    private static final long serialVersionUID = 1L;
    private MsgType type;

    //必须唯一，否则会出现channel调用混乱
    private String clientId;

    //初始化客户端id
    public BaseMsg() {
        this.clientId = Constants.getClientId();
    }

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
