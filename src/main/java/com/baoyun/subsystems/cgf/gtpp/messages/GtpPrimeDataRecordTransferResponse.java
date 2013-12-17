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
public class GtpPrimeDataRecordTransferResponse extends GtpPrimeMessage {

	/**
	 *
	 * @param cause
	 *            原因码值
	 */
	// 可能未来要添加TLV ，type=253的Requests Responded
	public GtpPrimeDataRecordTransferResponse(short cause) {

		super();
		GtpPrimeHeader hdr = getHeader();
		hdr.setMessageType(
				Constants.GTP_PRIME_DATA_RECORD_TRANSFER_RESPONSE);
		addInformationElement(new GtpPrimeInformationElementTV(
				Constants.GTP_PRIME_IET_CAUSE,
				cause));
//		// FIXME: Requests Responded added:
//		byte[] ietBin = new byte[5];
//
//		// T: 253
//		ietBin[0] = (byte) Constants.GTP_PRIME_IET_DATA_RECORD_REQUESTS_RESPONDED;
//
//		// L: 2 (bytes)
//		ietBin[1] = 2;
//		ietBin[2] = 0;
//
//		// V: (short) hdr.getSequenceNumber()
//		int seq = hdr.getSequenceNumber();
//		ietBin[3] = (byte) ((seq >> (8 * 3)) & 0xFF);
//		ietBin[4] = (byte) ((seq >> (8 * 2)) & 0xFF);
//
//		addInformationElement(new GtpPrimeInformationElementTLV(
//				Constants.GTP_PRIME_IET_DATA_RECORD_REQUESTS_RESPONDED,
//				ietBin));
//
//		addInformationElement(new GtpPrimeInformationElementTV(
//				Constants.GTP_PRIME_IET_DATA_RECORD_REQUESTS_RESPONDED,
//				(short) hdr.getSequenceNumber()));
	}

	/**
	 * @param header
	 * @param message
	 */
	public GtpPrimeDataRecordTransferResponse(GtpPrimeHeader header,
			byte[] message) {

		super(header, message);
	}

}
