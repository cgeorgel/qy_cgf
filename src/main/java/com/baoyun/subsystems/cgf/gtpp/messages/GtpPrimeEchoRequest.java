package com.baoyun.subsystems.cgf.gtpp.messages;

import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;


public class GtpPrimeEchoRequest extends GtpPrimeMessage {
	
	private static final short DEFAULT_RESTARTTIMER = 0;

	public GtpPrimeEchoRequest() {
		super();
	}
	
	public GtpPrimeEchoRequest(GtpPrimeHeader header, byte[] message) {
		super(header,message);	
	}
	
	public GtpPrimeMessage getResponse(short restartTimer) {
		GtpPrimeMessage msg = GtpPrimeMessageFactory.createEchoResponseMessage(restartTimer);
		msg.getHeader().setSequenceNumber(this.getHeader().getSequenceNumber()+1);
		return msg;		
	}
	
	public GtpPrimeMessage getResponse() {
		return getResponse(DEFAULT_RESTARTTIMER);
	}
	
}
