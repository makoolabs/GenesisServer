package com.genesis.gamedb.redis.key.model;

import com.genesis.gamedb.orm.entity.HumanEntity;
import com.genesis.gamedb.redis.key.IMultiRedisKey;
import com.genesis.gamedb.redis.key.RedisKeyType;
import com.genesis.gamedb.redis.key.RedisKeyWithServerId;
import com.genesis.gamedb.redis.redisop.IEntityRedisOp;
import com.genesis.gamedb.redis.redisop.model.EntityMultiKeyRedisOp;

public class HumanKey extends RedisKeyWithServerId<Long, HumanEntity>
        implements IMultiRedisKey<Long, HumanEntity> {

    public HumanKey(Integer serverId, Long id) {
        super(serverId, id);
    }

    @Override
    public String getKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(getServerId());
        sb.append(RedisKeyWithServerId.separator);
        sb.append(RedisKeyType.Human.toString());
        return sb.toString();
    }

    @Override
    public Class<HumanEntity> getEntityType() {
        return HumanEntity.class;
    }

    @Override
    public IEntityRedisOp getEntityRedisOp() {
        return EntityMultiKeyRedisOp.INSTANCE;
    }

    @Override
    public String getField() {
        return this.getDbId().toString();
    }

}
