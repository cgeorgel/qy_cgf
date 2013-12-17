package com.baoyun.subsystems.cgf;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.handler.CgfChannelPipelineFactory;

/**
 *
 * TODO: the structure of the CGF server: thread model, startup, shutdown, event-driven,
 * receive-decode-handle-encode-send cycle
 */
public class CgfServer {

	private static final Logger _log = LoggerFactory.getLogger(CgfServer.class);

	private ChannelFactory channelFactory;

	private Channel channel;

	public CgfServer() {

		channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
	}

	public void start() {

		ConnectionlessBootstrap bootStrap = new ConnectionlessBootstrap(channelFactory);

		bootStrap.setPipelineFactory(new CgfChannelPipelineFactory());
		bootStrap.setOption("broadcast", "false");
		bootStrap.setOption("receiveBufferSizePredictorFactory",
				new FixedReceiveBufferSizePredictorFactory(1024));
		bootStrap.setOption("sendBufferSize", 65535);
		bootStrap.setOption("receiveBufferSize", 65535);

		channel = bootStrap.bind(new InetSocketAddress(Constants.GTP_PORT));
	}

	public void stop() {

		if (channel != null && channel.isOpen()) {
			channel.close();
			channel = null;
		}

		channelFactory.releaseExternalResources();
	}

	public static void main(String[] args) {

		_log.debug("server starting................");
		CgfServer server = new CgfServer();
		server.start();
		_log.debug("server start ok!");
	}
}
