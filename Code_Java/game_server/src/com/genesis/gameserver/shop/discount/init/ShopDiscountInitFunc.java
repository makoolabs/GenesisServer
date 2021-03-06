package com.genesis.gameserver.shop.discount.init;

import com.genesis.gameserver.core.serverinit.ServerInitFunction;
import com.genesis.gamedb.orm.entity.ShopDiscountEntity;
import com.genesis.gamedb.redis.key.model.ShopDiscountKey;
import com.genesis.gameserver.core.global.ServerGlobals;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 商店打折初始化服务
 * @author yaguang.xiao
 *
 */
public class ShopDiscountInitFunc extends ServerInitFunction<ShopDiscountInitResult> {

    @Override
    public ShopDiscountInitResult apply(ServerGlobals sGlobals) {
        // 从Redis里面加载物品打折信息
        ShopDiscountKey shopDiscountKey = new ShopDiscountKey(sGlobals.getServerId(), 0l, 0);
        Map<String, ShopDiscountEntity> map = sGlobals.getRedis().getHashOp()
                .hgetall(shopDiscountKey.getKey(), ShopDiscountEntity.class).get();
        Collection<ShopDiscountEntity> shopDiscountEntities;
        if (map == null || map.isEmpty()) {
            shopDiscountEntities = Collections.emptyList();
        } else {
            shopDiscountEntities = map.values();
        }

        return new ShopDiscountInitResult(shopDiscountEntities);
    }

    @Override
    public void set(ShopDiscountInitResult result, ServerGlobals sGlobals) {
        sGlobals.getDiscountService().init(result.shopDiscounts, sGlobals);
    }

}
