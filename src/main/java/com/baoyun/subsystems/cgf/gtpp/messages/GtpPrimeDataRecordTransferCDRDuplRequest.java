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
public class GtpPrimeDataRecordTransferCDRDuplRequest extends
		GtpPrimeDataRecordTransferCDRRequestBase {

	/**
	 * 
	 */
	public GtpPrimeDataRecordTransferCDRDuplRequest() {
		super();
		this.addInformationElement(new GtpPrimeInformationElementTV(
				Constants.GTP_PRIME_IET_PACKET_TRANSFER_COMMAND,
				Constants.GTP_PRIME_IET_PTC_SEND_DUPLDATA));
	}

	/**
	 * @param header
	 * @param message
	 */
	public GtpPrimeDataRecordTransferCDRDuplRequest(GtpPrimeHeader header,
			byte[] message) {
		super(header, message);
		// TODO Auto-generated constructor stub
	}

}
