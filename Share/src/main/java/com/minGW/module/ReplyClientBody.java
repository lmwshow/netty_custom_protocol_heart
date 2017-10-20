package com.minGW.module;

/**
 * Created by Gracecoder on 2017/10/20.
 */
public class ReplyClientBody extends ReplyBody {
    private String clientInfo;

    public ReplyClientBody(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }
}
