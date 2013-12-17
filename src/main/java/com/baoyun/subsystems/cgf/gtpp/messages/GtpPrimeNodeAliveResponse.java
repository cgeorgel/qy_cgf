package com.baoyun.subsystems.cgf.gtpp.messages;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;

public class GtpPrimeNodeAliveResponse extends GtpPrimeMessage {

	public GtpPrimeNodeAliveResponse() {
		super();
		this.getHeader().setMessageType(
				Constants.GTP_PRIME_NODE_ALIVE_RESPONSE);
	}

	public GtpPrimeNodeAliveResponse(GtpPrimeHeader header, byte[] message) {
		super(header, message);
	}
	
}
