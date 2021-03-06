package com.genesis.gameserver.core.timeout.callback;

import com.genesis.gameserver.core.timeout.TimeoutCallbackManager;

import akka.actor.ActorRef;

/**
 * 超时回调的接口，该接口的实现可以注册到{@link TimeoutCallbackManager}中进行超时回调。<p>
 *
 * 注册到{@link TimeoutCallbackManager}之后，当超时发生时，{@code execute}方法会在
 * 指定的Actor中{@code getExecuteActor}执行。
 *
 * @author pangchong
 *
 */
public interface ITimeoutCallback {

    /**
     * 超时回调执行的入口方法
     */
    void execute();

    /**
     * 指定execute方法执行的线程
     * @return
     */
    ActorRef getExecuteActor();
}
