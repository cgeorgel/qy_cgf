/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElement;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTV;

/**
 *
 */
public class GtpPrimeEchoResponse extends GtpPrimeMessage {
	
	public GtpPrimeEchoResponse() {
		this((short)0);
	}
	
	public GtpPrimeEchoResponse(short restartCounter) {
		super();
		this.getHeader().setMessageType(Constants.GTP_PRIME_ECHO_RESPONSE);				
		this.addInformationElement(new GtpPrimeInformationElementTV(Constants.GTP_PRIME_IET_RECOVERY,restartCounter));		
	}
	
	public GtpPrimeEchoResponse(GtpPrimeHeader header, byte[] message) {
		super(header,message);	
	}
	
	public void setRestartCounter(short value) {
		GtpPrimeInformationElement el = getInformationElement(Constants.GTP_PRIME_IET_RECOVERY);
		((GtpPrimeInformationElementTV)el).setValue(value);
	}
	

}
