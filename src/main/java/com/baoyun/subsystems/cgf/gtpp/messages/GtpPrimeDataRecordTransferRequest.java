/**
 * <B>设置gtp消息头Constants.GTP_PRIME_DATA_RECORD_TRANSFER_REQUEST，用于传送CDR</B>
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;

/**
 *
 */
public class GtpPrimeDataRecordTransferRequest extends GtpPrimeMessage {

	public GtpPrimeDataRecordTransferRequest() {

		super();
		this.getHeader().setMessageType(Constants.GTP_PRIME_DATA_RECORD_TRANSFER_REQUEST);

		/*
		 * this.addInformationElement(new GtpPrimeInformationElementTV(
		 * GtpPrimeConstants.GTP_PRIME_IET_PACKET_TRANSFER_COMMAND,
		 * GtpPrimeConstants.GTP_PRIME_IET_PTC_CANCEL_DATA));
		 */
	}

	/**
	 *
	 * @param header
	 * @param message
	 *            输入流即为message
	 */
	public GtpPrimeDataRecordTransferRequest(GtpPrimeHeader header, byte[] message) {

		super(header, message);
	}

	public GtpPrimeDataRecordTransferResponse getResponse(short cause) {

		GtpPrimeDataRecordTransferResponse response = new GtpPrimeDataRecordTransferResponse(cause);
		// FIXME: why write header: sequenceNumber here, not during GtpPrimeDataRecordTransferResponse creation?
		// FIXME: +1?
		response.getHeader().setSequenceNumber(getHeader().getSequenceNumber()/* + 1*/);
		return response;
	}

}
