package com.genesis.gameserver.chat.cmd;

import com.genesis.gameserver.chat.cmd.core.IGmCmdFunction;
import com.genesis.gameserver.core.global.ServerGlobals;
import com.genesis.gameserver.human.Human;

import java.util.List;

/**
 * 给货币的GM命令的超类，负责验证参数的正确性。<p>
 *
 * 继承该类的子类直接定义GM命令，并且直接给对应的货币即可。
 *
 * @author pangchong
 *
 */
public abstract class AbstractGiveMoneyCmdFunc implements IGmCmdFunction {

    @Override
    public void handle(List<String> paramList, Human human, ServerGlobals sGlobals) {
        if (paramList == null || paramList.isEmpty() || paramList.size() < 1) {
            return;
        }

        long amount = Long.valueOf(paramList.get(0));

        if (amount == 0) {
            return;
        }

        giveMoney(human, amount);
    }

    public abstract void giveMoney(Human human, long amount);

}
