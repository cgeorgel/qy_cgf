/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;

/**
 *
 */
public class GtpPrimeVersionNotSupported extends GtpPrimeMessage {

	public GtpPrimeVersionNotSupported() {
		super();
		this.getHeader().setMessageType(Constants.GTP_PRIME_VERSION_NOT_SUPPORTED);
	}
	
	public GtpPrimeVersionNotSupported(GtpPrimeHeader header, byte[] message) {
		super(header,message);	
	}
	
}
