/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import java.net.InetSocketAddress;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTLV;

/**
 * 
 */
public class GtpPrimeNodeAliveRequest extends GtpPrimeMessage {

	private void addCGateway(String ip) {
		this.addInformationElement(new GtpPrimeInformationElementTLV(
				Constants.GTP_PRIME_IET_CHARGING_GATEWAY_ADDRESS,
				new InetSocketAddress(ip, 0).getAddress().getAddress()));
		
	}
	
	private void init(String ip) {
		this.getHeader().setMessageType(
				Constants.GTP_PRIME_NODE_ALIVE_REQUEST);
		addCGateway(ip);
	}

	public GtpPrimeNodeAliveRequest(String ip) {
		super();
		init(ip);
	}

	public GtpPrimeNodeAliveRequest(String ip1, String ip2) {
		super();
		init(ip1);
		addCGateway(ip2);
		
	}

	public GtpPrimeNodeAliveRequest(GtpPrimeHeader header, byte[] message) {
		super(header, message);
	}
	
	public GtpPrimeMessage getResponse() {
		GtpPrimeMessage msg = GtpPrimeMessageFactory.createNodeAliveResponseMessage();
		msg.getHeader().setSequenceNumber(this.getHeader().getSequenceNumber()+1);
		return msg;		
	}

}
