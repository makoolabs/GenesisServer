package com.genesis.gameserver.server;

import com.genesis.gameserver.arena.init.ArenaInitResult;
import com.genesis.gameserver.core.actor.ResumeSupervisorStrategyActor;
import com.genesis.gameserver.core.concurrent.ArgsCallable;
import com.genesis.gameserver.core.global.Globals;
import com.genesis.gameserver.core.global.ServerGlobals;
import com.genesis.gameserver.core.heartbeat.HeartbeatHelper;
import com.genesis.gameserver.core.heartbeat.StartHeartbeat;
import com.genesis.gameserver.player.PlayerManagerActor;
import com.genesis.gameserver.scene.SceneActor;
import com.genesis.gameserver.server.init.ServerActorInitResult;
import com.google.common.base.Optional;

import com.genesis.common.core.GlobalData;
import com.genesis.common.dailyreward.template.DailyRewardsTemplate;
import com.genesis.servermsg.core.isc.ServerType;
import com.genesis.servermsg.core.isc.msg.ServerMessage;
import com.genesis.servermsg.core.isc.remote.IRemote;
import com.genesis.servermsg.core.isc.remote.actorrefs.IActorPackages;
import com.genesis.servermsg.core.isc.remote.actorrefs.MultiTargetActorRefs;
import com.genesis.servermsg.core.isc.remote.actorrefs.annotation.MessageAcception;
import com.genesis.core.msgfunc.MsgArgs;
import com.genesis.core.util.TimeUtils;
import com.genesis.gameserver.arena.ArenaActor;
import com.genesis.gameserver.core.concurrent.AsyncArgs;
import com.genesis.gameserver.core.config.GameServerConfig;
import com.genesis.gameserver.core.global.ServerActorGlobals;
import com.genesis.gameserver.core.heartbeat.Heartbeat;
import com.genesis.gameserver.core.serverinit.ServerInitComplete;
import com.genesis.gameserver.core.serverinit.ServerInitFailException;
import com.genesis.gameserver.core.serverinit.ServerInitFailed;
import com.genesis.gameserver.core.serverinit.ServerInitFunction;
import com.genesis.gameserver.core.serverinit.ServerInitObject;
import com.genesis.protobuf.MessageType.MessageTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import akka.actor.ActorInitializationException;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.dispatch.OnComplete;

/**
 * 用于逻辑服务器的Actor。<p>
 *
 * 该Actor的监管策略为resume。因为从服务器的逻辑来说，任何可预测的异常（Exception及其子类）
 * 都不应该停止或者重启其内部系统的继续执行。但是当除了可预测的异常之外的Throwable发生时，
 * 意味着发生了严重的错误，此时停止整个ActorSystem也是可以接受的。
 *
 * @author pangchong
 *
 */
@MessageAcception(MessageTarget.SERVER)
public class ServerActor extends ResumeSupervisorStrategyActor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final GameServerConfig serverConfig;
    private final ServerGlobals sGlobals;

    private ActorRef scene = null;
    private ActorRef playerManager = null;
    private ActorRef arena = null;
    private IActorPackages localPackages = null;

    public ServerActor(GameServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.sGlobals = new ServerGlobals(this.serverConfig);
    }

    @Override
    public void preStart() throws Exception {
        //初始化本服务器所需的所有全局变量以及全局Actor
        Globals.getServerInitProcessUnit().submitTask(new ArgsCallable<ServerInitObject>() {
            @Override
            public ServerInitObject call(AsyncArgs args) throws Exception {
                //获取启动服务器的全局数据，一般这里需要从redis中获取
                Map<Class<?>, Object> map = new HashMap<>();
                for (Entry<Class<?>, ServerInitFunction<?>> each : Globals
                        .getServerInitFuncService().funcs.entrySet()) {
                    ServerInitFunction<?> eachFunc = each.getValue();
                    map.put(each.getKey(), eachFunc.apply(sGlobals));
                }
                return new ServerInitObject(map);
            }
        }).onComplete(new OnComplete<ServerInitObject>() {
            @Override
            public void onComplete(Throwable failure, ServerInitObject result) throws Throwable {
                UntypedActorContext context = ServerActor.this.getContext();
                if (failure != null) {
                    context.self().tell(new ServerInitFailed(failure), ActorRef.noSender());
                    return;
                }

                HashMap<Class<? extends UntypedActor>, ActorRef> map =
                        serverInitCompleteFunction(context, result);
                map.put(ServerManagerActor.class, context.parent());
                //打包发送到ServerActor容器中
                context.self()
                        .tell(new ServerInitComplete(map, sGlobals, result), ActorRef.noSender());
            }
        }, Globals.getServerInitProcessUnit().getExecutionContext());

    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof ServerInitComplete) {
            try {
                //检查服务器启动时间对应的当月奖励
                int currentMonth = TimeUtils.parseMonth(Globals.getTimeService().now());
                if (!GlobalData.getTemplateService()
                        .isTemplateExist(currentMonth, DailyRewardsTemplate.class)) {
                    throw new RuntimeException(
                            "Can not find daily rewards of month " + currentMonth +
                                    " of server start time!");
                }

                ServerInitComplete complete = (ServerInitComplete) msg;

                localPackages = new MultiTargetActorRefs(serverConfig.getServerType(),
                        serverConfig.getServerId(), Globals.getTargetClassMap(), complete.map);

                playerManager = getActorRefByType(complete, PlayerManagerActor.class);
                scene = getActorRefByType(complete, SceneActor.class);
                arena = getActorRefByType(complete, ArenaActor.class);
                ServerActorGlobals actorGlobals =
                        new ServerActorGlobals(playerManager, scene, arena);
                sGlobals.setActorGlobals(actorGlobals);

                // 启动时间轴
                HeartbeatHelper.registerHeartbeat(this.getSelf());
                // 给ServerGlobals进行赋值
                for (Entry<Class<?>, ServerInitFunction<?>> each : Globals
                        .getServerInitFuncService().funcs.entrySet()) {
                    ServerInitFunction<?> eachFunc = each.getValue();
                    eachFunc.set(complete.serverInitObject.get(each.getKey()), sGlobals);
                }

                //初始化完毕，通知PlayerManagerActor开始心跳
                sGlobals.getActorGlobals().playerManager.tell(StartHeartbeat.INSTANCE);

                //注册本服到DataServer
                for (int each : serverConfig.dataServerIds) {
                    registerToRemote(ServerType.DB, each);
                    sGlobals.getDataService().add(each,
                            sGlobals.getISCService().getRemote(ServerType.DB, each).get());
                }

                //注册本服到AgentServer
                registerToRemote(ServerType.GATE, serverConfig.getConnectedAgentServerId());
                log.info("Server {} has started!", this.sGlobals.getServerId());
            } catch (Exception e) {
                // 停止服务器
                throw new ActorInitializationException(this.self(),
                        "Server init failed! ID: [" + sGlobals.getServerId() + "]", e);
            }
        } else if (msg instanceof ServerInitFailed) {
            throw new ActorInitializationException(this.self(),
                    "Server init failed! ID: [" + sGlobals.getServerId() + "]",
                    ((ServerInitFailed) msg).getException());
        } else if (msg instanceof ServerMessage) {
            this.handleServerMsg((ServerMessage) msg);
        } else if (msg == Heartbeat.INSTANCE) {
            heartbeat();
        }
    }

    /**
     * 心跳
     */
    private void heartbeat() {
        try {
            this.sGlobals.getTimeAxis().heartbeat();
        } finally {
            HeartbeatHelper.registerHeartbeat(this.getSelf());
        }
    }

    private void handleServerMsg(ServerMessage sMsg) {
        Optional<IRemote> option = sGlobals.getISCService().getRemote(sMsg.sType, sMsg.sId);
        if (option.isPresent()) {
            Globals.getServerMsgFuncService()
                    .handle(MessageTarget.SERVER, option.get(), sMsg.msg, sGlobals,
                            MsgArgs.nullArgs);
        } else {
            log.warn(
                    "ServerActor receive a message [{}] can not find remote: ServerType:[{}], ServerId:[{}]",
                    sMsg.msg.getClass().getName(), sMsg.sType.name(), sMsg.sId);
        }
    }

    private void registerToRemote(ServerType sType, int sId) {
        if (sGlobals.getISCService().registerToRemote(sType, sId, localPackages)) {
            return;
        }
        throw new RuntimeException(
                "Can not register to server, Type: [" + sType + "], ID: [" + sId + "]");
    }

    private ActorRef getActorRefByType(ServerInitComplete complete,
            Class<? extends UntypedActor> actorType) {
        Optional<ActorRef> option = complete.get(actorType);
        if (!option.isPresent()) {
            throw new ServerInitFailException(actorType.getName() + " init failed!");
        }
        return option.get();
    }


    /**
     * 根据获取的数据，对该逻辑游戏服务器的全局Actor进行启动。
     *
     * @param context
     * @param result
     * @param redisProcessUnit
     * @return
     */
    private HashMap<Class<? extends UntypedActor>, ActorRef> serverInitCompleteFunction(
            UntypedActorContext context, ServerInitObject result) {
        ServerActorInitResult serverInitResult = result.get(ServerActorInitResult.class).get();
        HashMap<Class<? extends UntypedActor>, ActorRef> map = new HashMap<>();
        map.put(ServerActor.class, context.self());
        map.put(PlayerManagerActor.class, context.actorOf(
                Props.create(PlayerManagerActor.class, sGlobals, serverInitResult.humanInfoCache),
                PlayerManagerActor.ACTOR_NAME));
        map.put(SceneActor.class,
                context.actorOf(Props.create(SceneActor.class, sGlobals), SceneActor.ACTOR_NAME));
        map.put(ArenaActor.class, context.actorOf(
                Props.create(ArenaActor.class, sGlobals, result.get(ArenaInitResult.class).get()),
                ArenaActor.ACTOR_NAME));
        return map;
    }

}
