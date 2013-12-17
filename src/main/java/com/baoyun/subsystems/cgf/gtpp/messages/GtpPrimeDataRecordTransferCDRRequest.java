/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTV;

/**
 *添加GTP_PRIME_IET_PACKET_TRANSFER_COMMAND到iet list
 */
public class GtpPrimeDataRecordTransferCDRRequest extends
		GtpPrimeDataRecordTransferCDRRequestBase {

	public GtpPrimeDataRecordTransferCDRRequest() {
		super();
		this.addInformationElement(new GtpPrimeInformationElementTV(
				Constants.GTP_PRIME_IET_PACKET_TRANSFER_COMMAND,
				Constants.GTP_PRIME_IET_PTC_SEND_DATA));
	}

	public GtpPrimeDataRecordTransferCDRRequest(GtpPrimeHeader header,
			byte[] message) {
		super(header, message);
	}

}
