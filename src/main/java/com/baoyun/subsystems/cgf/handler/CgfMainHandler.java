package com.baoyun.subsystems.cgf.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTLV;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferRequest;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferResponse;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeEchoRequest;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeMessage;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeNodeAliveRequest;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeRedirectionRequest;
import com.baoyun.subsystems.cgf.handler.processor.CdrTransferRequestProcessor;
import com.baoyun.subsystems.cgf.utils.RepresentationDataUtils;

/**
 * bussiness logic dispatcher.
 *
 * @author george
 *
 */
public class CgfMainHandler extends SimpleChannelHandler {

	private static final Logger _log = LoggerFactory.getLogger(CgfMainHandler.class.getName());

	protected GtpDataTransferRequestProcessor dataTranProcessor;

	public CgfMainHandler() {

		dataTranProcessor = new CdrTransferRequestProcessor() {
		};
	}

	public void handleGtpPrimeMessage(GtpPrimeMessage msg) {

		_log.debug("Recognized Base Message");
	}

	public GtpPrimeMessage handleGtpPrimeMessage(GtpPrimeEchoRequest msg) {

		_log.debug("Recognized Echo Message");
		return msg.getResponse();
	}

	public GtpPrimeMessage handleGtpPrimeMessage(GtpPrimeNodeAliveRequest msg) {

		_log.debug("Recognized NodeAlive Message");
		return msg.getResponse();
	}

	public GtpPrimeMessage handleGtpPrimeMessage(
			GtpPrimeRedirectionRequest msg) {

		_log.debug("Recognized GtpPrimeRedirectionRequest Message");
		return msg.getResponse();
	}

	public GtpPrimeMessage handleGtpPrimeMessage(
			GtpPrimeDataRecordTransferRequest msg) {

		_log.debug("Recognized GtpPrimeDataRecordTransferRequest Message");
//		MemcachedClient client = CacheClientFactory.getCacheClient();

		try {
			dataTranProcessor.process(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// FIXME: 20130709: write appropriate cause value:
		GtpPrimeDataRecordTransferResponse resp = msg
				.getResponse(Constants.GTP_PRIME_IET_CAUSE_REQUEST_ACCEPTED);

		int reqSeq = msg.getHeader().getSequenceNumber();
		// FIXME: Requests Responded added:
//		byte[] ietBin = new byte[2];
//
//		ietBin[0] = (byte) ((reqSeq & 0xFF));
//		ietBin[1] = (byte) ((reqSeq >> (8 * 1)) & 0xFF);

		resp.addInformationElement(new GtpPrimeInformationElementTLV(
				Constants.GTP_PRIME_IET_DATA_RECORD_REQUESTS_RESPONDED,
				RepresentationDataUtils.short2ByteArray((short) reqSeq)));

		return resp;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event)
			throws Exception {

		Object msg = event.getMessage();
		GtpPrimeMessage response = null;
		if (msg instanceof GtpPrimeEchoRequest) {
			response = handleGtpPrimeMessage((GtpPrimeEchoRequest) msg);
		}
		if (msg instanceof GtpPrimeNodeAliveRequest) {
			response = handleGtpPrimeMessage((GtpPrimeNodeAliveRequest) msg);
		}
		if (msg instanceof GtpPrimeRedirectionRequest) {
			response = handleGtpPrimeMessage((GtpPrimeRedirectionRequest) msg);
		}
		if (msg instanceof GtpPrimeDataRecordTransferRequest) {
			response = handleGtpPrimeMessage((GtpPrimeDataRecordTransferRequest) msg);
		}

		if (response != null) {
			event.getChannel().write(response, event.getRemoteAddress());
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {

		_log.error(e.getCause().getLocalizedMessage());
		// TODO: better exception handling: level, capability, action, global effect
		e.getChannel().close();
	}
}
