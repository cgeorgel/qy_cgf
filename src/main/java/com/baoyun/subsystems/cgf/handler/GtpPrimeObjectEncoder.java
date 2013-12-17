package com.baoyun.subsystems.cgf.handler;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeMessage;

/**
 * <p>
 * GtpPrimeMessage -&gt; byte[].
 * </p>
 *
 * @author george
 *
 */
public class GtpPrimeObjectEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {

		if (msg instanceof GtpPrimeMessage) {
			return ChannelBuffers.copiedBuffer(((GtpPrimeMessage) msg).toByteArray());
		}
		return msg;

	}

}
