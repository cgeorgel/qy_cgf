package com.baoyun.subsystems.cgf.handler;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class CgfChannelPipelineFactory implements ChannelPipelineFactory {

// private SimpleChannelHandler handler;

	public CgfChannelPipelineFactory() {
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = Channels.pipeline();

		// p.addLast("logger", new LoggingHandler()); // TODO: FrameDecoder needed?
		p.addLast("encoder", new GtpPrimeObjectEncoder());
		p.addLast("decoder", new GtpPrimeObjectDecoder());

		p.addLast("handler", new CgfMainHandler());

		return p;
	}

}
