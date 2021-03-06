package com.genesis.gameserver.human.vip;

import com.genesis.common.core.GlobalData;
import com.genesis.common.human.HumanPropId;
import com.genesis.common.human.template.VipTemplate;
import com.genesis.common.human.vip.VipPrivilege;
import com.genesis.common.human.vip.VipPrivilegeType;
import com.genesis.core.util.MathUtils;
import com.genesis.gameserver.core.global.Globals;
import com.genesis.gameserver.human.Human;
import com.genesis.gameserver.human.vip.event.VipLevelUpEvent;
import com.genesis.protobuf.HumanMessage.GCVipLevelUp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * VIP处理类
 * @author ChangXiao
 *
 */
public class VipHelper {
    private static final Logger log = LoggerFactory.getLogger(VipHelper.class);

    /**
     * 增加VIP经验，并通知客户端
     *
     * @param human
     * @param addExp
     */
    public static void addExp(Human human, final long addExp) {
        checkArgument(addExp > 0, "参数有误：addExp必须大于0");

        Map<Integer, VipTemplate> vipLevelMap =
                GlobalData.getTemplateService().getAll(VipTemplate.class);

        //增加经验，溢出则抹平
        final long CURR_EXP = human.get(HumanPropId.VIP_EXP);
        long newExp = CURR_EXP + addExp;
        if (MathUtils.longAddOverflow(CURR_EXP, addExp)) {
            log.warn("玩家ID：{}，增加VIP经验方法溢出", human.getDbId());
            newExp = Long.MAX_VALUE;
        }

        final int CURR_LEVEL = human.getInt(HumanPropId.VIP_LEVEL);
        final int TOP_LEVEL = vipLevelMap.size() - 1;
        //判断是否升级
        int newLevel = CURR_LEVEL;
        for (int tmpLevel = CURR_LEVEL; tmpLevel < vipLevelMap.size(); tmpLevel++) {
            if (newExp < vipLevelMap.get(tmpLevel).getUpgradeExp() || tmpLevel == TOP_LEVEL) {
                newLevel = tmpLevel;
                break;
            }
        }

        human.set(HumanPropId.VIP_LEVEL, newLevel);
        human.set(HumanPropId.VIP_EXP, newExp);
        human.setModified();
        //执行升级事件
        if (newLevel > CURR_LEVEL) {
            Globals.getEventBus().occurs(new VipLevelUpEvent(human));
        }
        human.sendMessage(GCVipLevelUp.newBuilder().setVipLevel(newLevel).setVipExp(newExp));
    }

    /**
     * 获取Human的特权配置
     *
     * @param human
     * @param type
     * @return
     */
    public static VipPrivilege getVipPrivilege(Human human, VipPrivilegeType type) {
        checkNotNull(type);

        return GlobalData.getTemplateService()
                .get(human.getInt(HumanPropId.VIP_LEVEL), VipTemplate.class).getVipPrivileges(type);
    }

}
