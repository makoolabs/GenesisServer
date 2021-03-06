package com.genesis.gameserver.shop.funcs;

import com.genesis.common.shop.ShopType;
import com.genesis.gameserver.core.global.ServerGlobals;
import com.genesis.servermsg.core.isc.remote.IRemote;
import com.genesis.core.msgfunc.MsgArgs;
import com.genesis.servermsg.core.msgfunc.IServerMsgFunc;
import com.genesis.gamedb.uuid.UUIDType;
import com.genesis.gameserver.shop.discount.ShopDiscount;
import com.genesis.protobuf.MessageType.MessageTarget;
import com.genesis.servermsg.gameserver.shop.ShopDiscountInfo;

/**
 * 添加物品打折信息的处理器
 * @author Administrator
 *
 */

public class ShopDiscountInfoFunc
        implements IServerMsgFunc<ShopDiscountInfo, ServerGlobals, MsgArgs> {

    @Override
    public void handle(IRemote remote, ShopDiscountInfo msg, ServerGlobals sGlobals, MsgArgs arg2) {
        if (msg.serverId != sGlobals.getServerId()) {
            return;
        }

        ShopType shopType = ShopType.getByIndex(msg.shopTypeId);
        if (shopType == null) {
            return;
        }

        if (msg.discount <= 0) {
            return;
        }

        if (msg.numMultiple < 1) {
            return;
        }

        if (msg.startTime == null || msg.endTime == null) {
            return;
        }

        if (msg.startTime.getTime() > msg.endTime.getTime()) {
            return;
        }

        long nextShopDiscountUUID = sGlobals.getUUIDGenerator().getNextUUID(UUIDType.ShopDiscount);

        ShopDiscount shopDiscount =
                new ShopDiscount(sGlobals, nextShopDiscountUUID, msg.serverId, shopType,
                        msg.discount, msg.numMultiple, msg.startTime.getTime(),
                        msg.endTime.getTime());

        sGlobals.getDiscountService().addDiscountInfo(shopDiscount, sGlobals);
    }

    @Override
    public MessageTarget getTarget() {
        return MessageTarget.SERVER;
    }

}
