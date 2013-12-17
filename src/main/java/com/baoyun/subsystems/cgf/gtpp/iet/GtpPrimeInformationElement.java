/**
 * <B>返回IE的type，设置type，输出IE成byte数组</B>
 */
package com.baoyun.subsystems.cgf.gtpp.iet;

/**
 *
 */
public abstract class GtpPrimeInformationElement {

	protected short type = 0;
	
	public GtpPrimeInformationElement(short type) {
		this.type = type;
	}
	
	public short getIEType() {
		return type;
	}
	
	public abstract byte[] toByteArray();
	
}
