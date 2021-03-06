package com.genesis.test.dataserver;

import com.genesis.dataserver.globals.Globals;
import com.genesis.gamedb.orm.entity.ItemEntity;
import com.genesis.gamedb.redis.DbOp;
import com.genesis.gamedb.redis.DirtyDataInfo;
import com.genesis.gamedb.redis.key.SpecialRedisKeyBuilder;
import com.genesis.gamedb.redis.key.model.ItemKey;
import com.genesis.gameserver.core.persistance.IDataUpdater;
import com.genesis.gameserver.item.Item;
import com.genesis.test.dataserver.gamedb.ItemTable;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DataServerDirtyDataTest extends AbstractTest {
    @Test
    public void dirty_data_should_be_saved()
            throws InterruptedException, IOException, NoSuchMethodException, SecurityException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        //准备数据
        new ItemTable().insertToDB();

        //启动DataServer
        Globals.init();

        ItemEntity itemEntity = dbService.getById(ItemEntity.class, 2L);
        int oldOverlap = itemEntity.getOverlap();
        Constructor<Item> cons = Item.class.getConstructor(IDataUpdater.class);
        if (cons == null) {
            Assert.assertFalse(true);
        }
        cons.setAccessible(true);
        Item item = cons.newInstance(new MockDataUpdater());
        item.fromEntity(itemEntity);
        item.setOverlap(item.getOverlap() + 1);
        ItemEntity newEntity = item.toEntity();
        ItemKey itemKey = newEntity.newRedisKey(2001);

        redis.getHashOp().hset(itemKey.getKey(), itemKey.getField(), newEntity);
        String dirtyKey = SpecialRedisKeyBuilder.buildDirtyDataKey(2001);
        DirtyDataInfo info = new DirtyDataInfo();
        info.setOperateType(DbOp.UPDATE);
        info.setRedisKey(itemKey);
        redis.getListOp().lpush(dirtyKey, info);

        assertThat(dbService.getById(ItemEntity.class, 2L).getOverlap(), is(oldOverlap));
        Thread.sleep(11 * 1000);
        assertThat(dbService.getById(ItemEntity.class, 2L).getOverlap(), is(oldOverlap + 1));
    }
}
