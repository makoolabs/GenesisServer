package com.genesis.servermsg.agentserver;

import com.genesis.servermsg.core.msg.IMessage;
import com.genesis.protobuf.MessageType.MessageTarget;

import java.io.Serializable;

import akka.actor.ActorRef;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerLogined implements IMessage, Serializable {

    private static final long serialVersionUID = 1L;

    public final long agentSessionId;

    public final long humanUuid;

    public final int gameServerId;

    public final ActorRef playerActorRef;

    public PlayerLogined(long sessionId, long humanUuid, int serverId, ActorRef self) {
        this.agentSessionId = sessionId;
        this.humanUuid = humanUuid;
        this.gameServerId = serverId;
        this.playerActorRef = checkNotNull(self);
    }

    @Override
    public MessageTarget getTarget() {
        return MessageTarget.ISC_ACTOR;
    }

}
