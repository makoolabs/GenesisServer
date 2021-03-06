package com.genesis.gameserver.dailyreward.funcs;

import com.genesis.common.core.GlobalData;
import com.genesis.common.dailyreward.template.DailyRewardElemTemplate;
import com.genesis.common.dailyreward.template.DailyRewardsTemplate;
import com.genesis.gameserver.core.global.Globals;
import com.genesis.gameserver.core.global.ServerGlobals;
import com.genesis.gameserver.core.msgfunc.AbstractClientMsgFunc;
import com.genesis.gameserver.dailyreward.DailyRewardManager;
import com.genesis.gameserver.human.Human;
import com.genesis.core.util.TimeUtils;
import com.genesis.gameserver.player.Player;
import com.genesis.protobuf.DailyRewardMessage.CGGetDailyReward;
import com.genesis.protobuf.DailyRewardMessage.GCGetDailyRewardFailed;

/**
 * 获取当日对应的每日奖励（签到奖励）的函数对象。<p>
 *
 * 该函数对象在PlayerActor中执行。
 *
 * @author pangchong
 *
 */
public class CGGetDailyRewardFunc
        extends AbstractClientMsgFunc<CGGetDailyReward, Human, ServerGlobals> {

    @Override
    public void handle(Player player, CGGetDailyReward msg, Human human, ServerGlobals sGlobals) {
        long now = Globals.getTimeService().now();
        DailyRewardManager rewardManager = human.getDailyRewardManager();
        //根据上次签到时间判断当前是否隔天已经可以领奖
        if (!rewardManager.canGetReward()) {
            return;
        }
        int currentRewardIndexInClient = msg.getRewardIndex();
        int currentRewardIndexInServer = rewardManager.getTimesOfReward();
        if (currentRewardIndexInClient != currentRewardIndexInServer) {
            human.sendMessage(GCGetDailyRewardFailed.getDefaultInstance());
            rewardManager.sendDailyRewardInfo();
            return;
        }
        rewardManager.markRewardReceived(now);
        //根据月份获取当月的签到奖励
        DailyRewardsTemplate dailyRewardsTemplate = GlobalData.getTemplateService()
                .get(TimeUtils.parseMonth(now), DailyRewardsTemplate.class);
        //判断当前签到次数和给予英雄的签到次数是否相同
        if (currentRewardIndexInServer != rewardManager.getIndexOfHero()) {
            //不相同，则根据当前领奖的情况，给奖励
            int[] rewards = dailyRewardsTemplate.getRewardIds();
            DailyRewardElemTemplate rewardTemplate = GlobalData.getTemplateService()
                    .get(rewards[currentRewardIndexInServer], DailyRewardElemTemplate.class);
        } else {
            //相同，给英雄
            human.getHeroManager().addHeroByTemplateId(dailyRewardsTemplate.getHeroId());
        }
    }

}
