package com.genesis.core.akka;

import com.genesis.core.akka.config.AkkaConfig;
import com.genesis.core.akka.config.AkkaConfigBuilder;

import akka.actor.ActorSystem;

import static com.google.common.base.Preconditions.checkNotNull;

public class Akka {

    public final static String ACTOR_SYSTEM_NAME = "ActorSystem";

    @SuppressWarnings("unused")
    private final AkkaConfig akkaConfig;

    private final ActorSystem actorSystem;


    public Akka(AkkaConfig akkaConfig) {
        checkNotNull(akkaConfig, "Akka can not init with null config");
        this.akkaConfig = akkaConfig;
        actorSystem = ActorSystem.create(ACTOR_SYSTEM_NAME,
                AkkaConfigBuilder.build(akkaConfig.ip, akkaConfig.port).resolve());
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

}
