package com.genesis.gameserver.server;

import com.genesis.gamedb.orm.entity.ServerStatusEntity;
import com.genesis.gameserver.core.global.ServerGlobals;
import com.genesis.gameserver.core.persistance.ObjectInSqlImplNoCache;

import java.sql.Timestamp;

public class ServerStatus extends ObjectInSqlImplNoCache<Integer, ServerStatusEntity> {

    /**服务器ID*/
    private int serverId;
    /**开服时间*/
    private long serverOpenTime;

    public ServerStatus(ServerGlobals sGlobals) {
        super(sGlobals);
    }

    @Override
    public Integer getDbId() {
        return serverId;
    }

    @Override
    public ServerStatusEntity toEntity() {
        ServerStatusEntity entity = new ServerStatusEntity();
        entity.setServerId(serverId);
        entity.setServerOpenTime(new Timestamp(serverOpenTime));
        return entity;
    }

    @Override
    public void fromEntity(ServerStatusEntity entity) {
        if (entity == null) {
            return;
        }
        serverId = entity.getServerId();
        serverOpenTime = entity.getServerOpenTime().getTime();
    }

    public long getServerOpenTime() {
        return serverOpenTime;
    }
}