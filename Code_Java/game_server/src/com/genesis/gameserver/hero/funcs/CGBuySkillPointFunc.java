package com.genesis.gameserver.hero.funcs;

import com.genesis.gameserver.core.global.ServerGlobals;
import com.genesis.gameserver.human.Human;
import com.genesis.gameserver.core.msgfunc.playeractor.PlayerActorMsgFunc;
import com.genesis.gameserver.player.Player;
import com.genesis.protobuf.HeroMessage.CGBuySkillPoint;

public class CGBuySkillPointFunc extends PlayerActorMsgFunc<CGBuySkillPoint> {

    @Override
    public void process(Player player, CGBuySkillPoint msg, Human human, ServerGlobals sGlobals) {
        // TODO Auto-generated method stub
        //检查技能点是否已经满了

        //检查是否有此权限（如果买技能点是VIP特权的话）

        //检查钻石是否够


        //扣钻石

        //给技能点

        human.setModified();
    }

}
