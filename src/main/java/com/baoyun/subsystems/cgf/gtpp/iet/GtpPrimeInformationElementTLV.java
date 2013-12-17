/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.iet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;


/**
 * 
 */
public class GtpPrimeInformationElementTLV extends GtpPrimeInformationElement {

	byte[] arr;
	
	private int convertByteToInt(byte a) {
		return a & 0x00FF;
	}
	
	
	/**
	 *@
	 */
	//可能存在问题，和GtpPrimeHeader相比较，length需要转换convertToInt()，已解决
	public GtpPrimeInformationElementTLV(byte[] arr, int startIndex) {
		super(arr[startIndex]);
		int length = (convertByteToInt(arr[startIndex+1]) << 8) + convertByteToInt(arr[startIndex+2]);		
		this.arr = Arrays.copyOfRange(arr, startIndex+3, startIndex+3+length);
	}
	//直接将type和内容添加，无需管length
	public GtpPrimeInformationElementTLV(short type, byte[] arr) {
		super(type);
		this.arr = arr;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.protocol.gtp.prime.GtpPrimeInformationElement#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			out.write((byte)type);
			out.write((arr.length >> 8 ) & 0xff);
			out.write(arr.length & 0xff);
			out.write(arr);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.toByteArray();
	}

	public int getTotalSize() {
		return arr.length + 3;
	}
	
}
