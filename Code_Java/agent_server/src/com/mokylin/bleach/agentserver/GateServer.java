package com.mokylin.bleach.agentserver;

import com.mokylin.bleach.agentserver.channel.AgentServerChannelListener;
import com.mokylin.bleach.agentserver.core.global.Globals;
import com.mokylin.bleach.agentserver.core.net.codec.ClientToAgentMessageDecoder;
import com.mokylin.bleach.agentserver.handlers.AgentClientMessageHandler;
import com.mokylin.bleach.core.config.model.NetInfo;
import com.mokylin.bleach.core.net.NettyNetworkLayer;
import com.mokylin.td.network2client.core.channel.ChannelInitializerImpl;
import com.mokylin.td.network2client.core.handle.ServerIoHandler;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网关服务器
 * <p>2018-03-06 10:57
 *
 * @author Joey
 **/
public class GateServer {

    private static Logger log = LoggerFactory.getLogger(GateServer.class);

    public static void main(String[] args) {

        try {
            Globals.init();

            // 启动用于监听GameServer消息的Netty
            startNettyToGame();

            // 启动用于监听客户端消息的Netty
            startNettyToClient();

            log.info("GateServer 启动成功！");
        } catch (Exception e) {
            log.error("GateServer started fail!!! Cause by: ", e);
            System.exit(-2);
        }
    }

    /**
     * 启动用于监听Client消息的Netty网络层
     */
    private static void startNettyToClient() throws Exception {
        NetInfo netInfoToClient = Globals.getGateConfig().getNetInfoToClient();
        AgentClientMessageHandler mp = new AgentClientMessageHandler();
        AgentServerChannelListener rs = new AgentServerChannelListener();
        ServerIoHandler handler = new ServerIoHandler(mp, rs);
        ChannelHandler childHandler =
                new ChannelInitializerImpl(handler, ClientToAgentMessageDecoder.class);

        NettyNetworkLayer.configNet(netInfoToClient.getHost(), netInfoToClient.getPort())
                .start(childHandler);

        //////////////////////////////////////////////////////////////
//        LoginClientMessageHandler mp = new LoginClientMessageHandler(
//                new FixThreadPool(50, new ActionOnExceptionOfLogin()));
//        LoginClientChannelListener rs = new LoginClientChannelListener();
//        ClientIoHandler handler = new ClientIoHandler(mp, rs);
//        ChannelHandler childHandler = new ChannelInitializerImpl(handler, ClientToLoginMessageDecoder.class);
//        NettyNetworkLayer.configNet(netInfoToClient.getHost(), netInfoToClient.getPort()).start(childHandler);
    }

    /**
     * 启动用于监听GameServer消息的Netty网络层
     */
    private static void startNettyToGame() {
        // TODO
        NetInfo netInfoToMapServer = Globals.getGateConfig().getNetInfoToGame();
        //NettyNetworkLayer.configNet(netInfoToMapServer.getHost(), netInfoToMapServer.getPort());
        //			.addMessageProcess(mp, rs)
        //			.start();
    }
}
