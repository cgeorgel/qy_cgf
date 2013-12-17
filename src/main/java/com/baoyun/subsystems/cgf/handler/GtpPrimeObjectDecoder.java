package com.baoyun.subsystems.cgf.handler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeMessageFactory;

/**
 * <p>
 * byte[] -&gt; GtpPrimeMessage.
 * </p>
 *
 * @author george
 *
 */
public class GtpPrimeObjectDecoder extends OneToOneDecoder {

	private static Logger log = LoggerFactory.getLogger(GtpPrimeObjectDecoder.class);

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object obj) throws Exception {

		if (obj instanceof ChannelBuffer) {

			log.trace("Received message");
			ChannelBuffer buf = (ChannelBuffer) obj;
			byte[] msg = new byte[buf.readableBytes()];
			buf.readBytes(msg);
			return GtpPrimeMessageFactory.decodeFromByteArray(msg);
		}

		return obj;
	}

}
