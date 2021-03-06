package com.genesis.dataserver.redis;

import com.genesis.dataserver.globals.Globals;
import com.genesis.core.config.Mapping;
import com.genesis.core.redis.IRedis;
import com.genesis.core.redis.RedisService;
import com.genesis.core.redis.config.RedisConfig;

/**
 * DataServer的Redis连接管理器，会访问多个Redis
 * @author Joey
 *
 */
public class RedisManager {

    private final RedisService redisService;

    public RedisManager() {
        redisService = new RedisService(null,
                RedisConfig.getRedisConfigs().values().toArray(new RedisConfig[0]));
    }

    /**
     * 根据原服务器ID取IRedis
     * @param originalServerId
     * @return
     */
    public IRedis getIRedis(Integer originalServerId) {
        Mapping mapConf = Globals.getServerConfig().mappingConf;
        Integer currentServerId = mapConf.getOriginalgs_currentgs_map().get(originalServerId);
        String redisName = mapConf.getGs_redis_map().get(currentServerId);

        return redisService.getRedis(redisName).get();
    }

}
