package com.genesis.test.gameserver.timeaxis;

import com.genesis.core.concurrent.process.CommonProcessType;
import com.genesis.core.heartbeat.HeartbeatService;
import com.genesis.core.time.TimeService;
import com.genesis.core.timeaxis.ITimeEvent;
import com.genesis.core.timeaxis.TimeAxis;
import com.genesis.test.gameserver.timeaxis.task.SimpleTimeAxisEventImpl;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * 时间轴的测试
 *
 * @author yaguang.xiao
 *
 */
public class TimeAxisTest {

    @Test
    public void time_axis_task_should_run_in_period() {
        HeartbeatService.INSTANCE.start(CommonProcessType.MAIN, 100); // 启动心跳服务
        final TimeService ts = TimeService.Inst; // 启动时间服务
        CommonProcessType.SCHEDULED.schedule(new Runnable() {
            @Override
            public void run() {
                ts.heartbeat();
            }
        }, 500, TimeUnit.MILLISECONDS);

        Human human = new Human();
        final TimeAxis<Human> timeAxis = new TimeAxis<Human>(ts, human); // 创建时间轴，宿主为Human
        HeartbeatService.INSTANCE.registerHeartbeat(timeAxis); // 注册该时间轴到心跳服务中

        // 在主线程中调度一个时间轴 任务
        CommonProcessType.MAIN.submitTask(new Runnable() {

            @Override
            public void run() {
                ITimeEvent<Human> task = new SimpleTimeAxisEventImpl();
                timeAxis.scheduleEventAfterMS(task, 1000, 1000);
            }

        });

        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertArrayEquals(new int[]{1}, new int[]{SimpleTimeAxisEventImpl.eventOccured});

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Assert.assertArrayEquals(new int[]{2}, new int[]{SimpleTimeAxisEventImpl.eventOccured});
    }

}
