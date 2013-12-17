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
public class GtpPrimeDataRecordTransferSequenceCancel extends
		GtpPrimeDataRecordTransferSequenceRequestBase {

	/**
	 * 
	 */
	public GtpPrimeDataRecordTransferSequenceCancel() {
		super();
		this.addInformationElement(new GtpPrimeInformationElementTV(
				Constants.GTP_PRIME_IET_PACKET_TRANSFER_COMMAND,
				Constants.GTP_PRIME_IET_PTC_CANCEL_DATA));

	}

	/**
	 * @param header
	 * @param message
	 */
	public GtpPrimeDataRecordTransferSequenceCancel(GtpPrimeHeader header,
			byte[] message) {
		super(header, message);		
	}

}
