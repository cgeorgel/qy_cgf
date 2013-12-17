/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTV;

/**
 *
 */
public class GtpPrimeRedirectionResponse extends GtpPrimeMessage {

	public GtpPrimeRedirectionResponse(short cause) {
		super();
		this.getHeader().setMessageType(
				Constants.GTP_PRIME_REDIRECTION_RESPONSE);
		this.addInformationElement(new GtpPrimeInformationElementTV(
				Constants.GTP_PRIME_IET_CAUSE,
				cause));				
	}

	public GtpPrimeRedirectionResponse(GtpPrimeHeader header, byte[] message) {
		super(header, message);
	}
	
}
