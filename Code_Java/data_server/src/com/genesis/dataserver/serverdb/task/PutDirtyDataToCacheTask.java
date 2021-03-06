package com.genesis.dataserver.serverdb.task;

import com.genesis.dataserver.serverdb.ServerDBManager;
import com.genesis.gamedb.redis.DirtyDataInfo;

public class PutDirtyDataToCacheTask implements Runnable {

    private ServerDBManager dbm;
    private DirtyDataInfo dirtyDataInfo;

    public PutDirtyDataToCacheTask(ServerDBManager dbm, DirtyDataInfo dirtyDataInfo) {
        this.dbm = dbm;
        this.dirtyDataInfo = dirtyDataInfo;
    }

    @Override
    public void run() {
        dbm.getDataCache().put(dirtyDataInfo);
    }

}
