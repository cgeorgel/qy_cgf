/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.helpers.SequenceProvider;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTLV;

/**
 *
 */
public class GtpPrimeDataRecordTransferSequenceRequestBase extends
		GtpPrimeDataRecordTransferRequest {

	public GtpPrimeDataRecordTransferSequenceRequestBase() {
		super();
	}

	public GtpPrimeDataRecordTransferSequenceRequestBase(GtpPrimeHeader header,
			byte[] message) {
		super(header, message);
	}

	protected void addSequences(short code, SequenceProvider provider) {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		List<Integer> list = provider.getSequenceList();
		for (Integer i : list) {
			out.write((i>> 8) & 0xFF);
			out.write(i & 0xFF);
		}
		
		this.addInformationElement(new GtpPrimeInformationElementTLV(code, out.toByteArray()));
	}
	
	public void addCancelledSequences(SequenceProvider provider) {
		this.addSequences(Constants.GTP_PRIME_IET_SEQUENCE_NUMBERS_CANCELLED, provider);		
	}
	

	public void addReleasedSequences(SequenceProvider provider) {
		this.addSequences(Constants.GTP_PRIME_IET_SEQUENCE_NUMBERS_RELEASED, provider);
	}
		
}
