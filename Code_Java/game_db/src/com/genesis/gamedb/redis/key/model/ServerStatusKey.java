package com.genesis.gamedb.redis.key.model;

import com.genesis.gamedb.orm.entity.ServerStatusEntity;
import com.genesis.gamedb.redis.key.RedisKeyType;
import com.genesis.gamedb.redis.key.RedisKeyWithServerId;
import com.genesis.gamedb.redis.key.AbstractRedisKey;
import com.genesis.gamedb.redis.redisop.IEntityRedisOp;
import com.genesis.gamedb.redis.redisop.model.EntityValueRedisOp;

public class ServerStatusKey extends AbstractRedisKey<Integer, ServerStatusEntity> {

    public ServerStatusKey(Integer id) {
        super(id);
    }

    @Override
    public String getKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(getServerId());
        sb.append(RedisKeyWithServerId.separator);
        sb.append(RedisKeyType.ServerStatus.toString());
        return sb.toString();
    }

    @Override
    public Class<ServerStatusEntity> getEntityType() {
        return ServerStatusEntity.class;
    }

    @Override
    public IEntityRedisOp getEntityRedisOp() {
        return EntityValueRedisOp.INSTANCE;
    }

    @Override
    public Integer getServerId() {
        return this.getDbId();
    }
}
