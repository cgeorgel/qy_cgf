/**
 * 
 */
package com.baoyun.subsystems.cgf.gtpp.iet;


/**
 *
 */
public class GtpPrimeInformationElementTV extends GtpPrimeInformationElement {

	protected short value = 0;
	
	public GtpPrimeInformationElementTV(short type, short value) {
		super(type);
		this.value = value;
	}
	
	@Override
	public byte[] toByteArray() {
		byte[] tmp = new byte[2];
		tmp[0] = (byte)(type & 0xFF);
		tmp[1] = (byte)(value & 0xFF);
		return tmp;
	}
	
	public void setValue(short value) {
		this.value = value;
	}

}
