package com.genesis.servermsg.loginserver;

import com.genesis.servermsg.core.msg.IMessage;
import com.genesis.protobuf.MessageType.MessageTarget;

public class PlayerLogout implements IMessage {

    /**账号ID*/
    public final String accountId;
    /**渠道*/
    public final String channel;

    public PlayerLogout(String accountId, String channel) {
        this.accountId = accountId;
        this.channel = channel;
    }

    @Override
    public MessageTarget getTarget() {
        return MessageTarget.ISC_ACTOR;
    }
}
