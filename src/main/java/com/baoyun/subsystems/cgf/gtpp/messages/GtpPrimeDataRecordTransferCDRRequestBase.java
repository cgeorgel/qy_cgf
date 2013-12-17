/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.helpers.CDRProvider;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTLV;

/**
 *主要用于添加CDR到GtpMessage的iet list
 */
public class GtpPrimeDataRecordTransferCDRRequestBase extends
		GtpPrimeDataRecordTransferRequest {

	public GtpPrimeDataRecordTransferCDRRequestBase() {
		super();
	}

	public GtpPrimeDataRecordTransferCDRRequestBase(GtpPrimeHeader header,
			byte[] message) {
		super(header, message);
	}
	
	public void addDataRecords(CDRProvider provider) throws Exception {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		List<byte[]> list = provider.getCDRs();
		
		out.write(list.size() & 0xFF);
		out.write(provider.getDataRecordFormat() & 0xFF);
		int version = provider.getDataRecordFormatVersion();
		out.write((version >> 8) & 0xFF);
		out.write(version & 0xFF);
		
		for (byte[] cdr : list) {
			out.write((cdr.length >> 8) & 0xFF);
			out.write(cdr.length & 0xFF);
			out.write(cdr);
		}
		
		this.addInformationElement(new GtpPrimeInformationElementTLV(Constants.GTP_PRIME_IET_DATA_RECORD_PACKET, out.toByteArray()));
		
	}


}
