/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import java.net.InetSocketAddress;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTLV;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTV;

/**
 *
 */
public class GtpPrimeRedirectionRequest extends GtpPrimeMessage {

	private void addCGateway(String ip) {
		this.addInformationElement(new GtpPrimeInformationElementTLV(
				Constants.GTP_PRIME_IET_CHARGING_GATEWAY_ADDRESS,
				new InetSocketAddress(ip, 0).getAddress().getAddress()));
		
	}
	
	private void init(short cause, String ip) {
		this.getHeader().setMessageType(
				Constants.GTP_PRIME_REDIRECTION_REQUEST);
		this.addInformationElement(new GtpPrimeInformationElementTV(
				Constants.GTP_PRIME_IET_CAUSE,
				cause));
		addCGateway(ip);
	}

	public GtpPrimeRedirectionRequest(short cause, String ip) {
		super();
		init(cause, ip);
	}

	public GtpPrimeRedirectionRequest(short cause, String ip1, String ip2) {
		super();
		init(cause, ip1);
		addCGateway(ip2);
		
	}
	
	public GtpPrimeRedirectionRequest(GtpPrimeHeader header, byte[] message) {
		super(header,message);	
	}
	
	public GtpPrimeMessage getResponse() {
		GtpPrimeMessage msg = GtpPrimeMessageFactory.createRedirectionResponseMessage(Constants.GTP_PRIME_IET_CAUSE_REQUEST_ACCEPTED);
		msg.getHeader().setSequenceNumber(this.getHeader().getSequenceNumber()+1);
		return msg;		
	}
	
}
