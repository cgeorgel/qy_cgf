/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.baoyun.subsystems.cgf.gtpp.header.GtpPrimeHeader;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElement;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTLV;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElementTV;

/**
 *
 */
public class GtpPrimeMessage {

	protected GtpPrimeHeader header;
	//iet用于存储TV或者TLV的集合
	protected ArrayList<GtpPrimeInformationElement> iet;
	
	public GtpPrimeMessage() {
		header = new GtpPrimeHeader();
		iet = new ArrayList<GtpPrimeInformationElement>();
	}
	
	public GtpPrimeMessage(GtpPrimeHeader header, byte[] message) {
		this.header = header;
		decodeIets(message, 6);
	}
	//
	private void decodeIets(byte[] message, int start) {
		if (start >= message.length) return;
		byte type = message[start];
		
		// TLV
		if (((type >> 7)&0x01) == 1) {
			GtpPrimeInformationElementTLV tlv = new GtpPrimeInformationElementTLV(message,start);
			addInformationElement(tlv);			
			start += tlv.getTotalSize();								
		} else {
			// TV
			addInformationElement(new GtpPrimeInformationElementTV((short)type,(short)message[start+1]));
			start += 2;
		}
		
		if (start < message.length) decodeIets(message, start);
	}
	//将gtpMessage输出成为byte数组
	public byte[] toByteArray() {		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream out_iets = new ByteArrayOutputStream();
		try {
			for (GtpPrimeInformationElement g: iet) {
				out_iets.write(g.toByteArray());				
			}
			header.setLength((short)out_iets.size());
			out.write(header.toByteArray());
			out.write(out_iets.toByteArray());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return out.toByteArray();
	}

	public GtpPrimeHeader getHeader() {
		return header;
	}

	public void setHeader(GtpPrimeHeader header) {
		this.header = header;
	}
	
	public void addInformationElement(GtpPrimeInformationElement el) {
		if (this.iet == null) this.iet = new ArrayList<GtpPrimeInformationElement>();
		this.iet.add(el);
	}
	/**
	 * 
	 * @param type IE的类型
	 * @return 返回IE的内容
	 */
	public GtpPrimeInformationElement getInformationElement(short type) {
		
		GtpPrimeInformationElement tmp = null;
		for (GtpPrimeInformationElement g : iet) {
			if (g.getIEType() == type ) {
				tmp = g;
				break;
			}
		}
		return tmp;
	}
	
	
}
