package com.baoyun.subsystems.cgf.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferResponse;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeEchoResponse;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeVersionNotSupported;

public class ClientReceiveGtppMessageHandler extends SimpleChannelHandler {

	private static final Logger _log = LoggerFactory
			.getLogger(ClientReceiveGtppMessageHandler.class.getName());

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event)
			throws Exception {

		Object msg = event.getMessage();
		if (msg instanceof GtpPrimeEchoResponse) {
			_log.info("recognized Echo Response message...");
			// GtpPrimeMessage response =
			// handleGtpPrimeMessage((GtpPrimeEchoRequestMessage)msg);
			// event.getChannel().write(response,event.getRemoteAddress());
		}

		if (msg instanceof GtpPrimeDataRecordTransferResponse) {
			_log.info("recognized Data Record Transfer Response message...");

		}

		if (msg instanceof GtpPrimeVersionNotSupported) {
			int version = ((GtpPrimeVersionNotSupported) msg).getHeader().getVersion();
			_log.info("recognized VersionNotSupported... Latest version supported is: {}.", version);

		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {

		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
