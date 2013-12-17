package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.net.InetAddress;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

public class SgwCdrCategoryKey {
	
	private static String pattern = "S-GW CDRs(category key): {chargingID=%s, p-GWAddress=%s,ratType=%s}";

	
	private long chargingId;

	private InetAddress pgwAddress;
	
	private int ratType;

	private String stringRepr;
	
	
	public SgwCdrCategoryKey(){
		
	}
	
	public SgwCdrCategoryKey(InputSgwCdrObject inputSgwCdr){
		chargingId = inputSgwCdr.getChargingId();
		pgwAddress = inputSgwCdr.getPgwAddress();
		ratType = inputSgwCdr.getRatType();
	}
	
	@Override
	public String toString() {

		if (stringRepr != null) {
			return stringRepr;
		} else {
			return genStringRepresentation();
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (chargingId ^ (chargingId >>> 32));
		result = prime * result + ((pgwAddress == null) ? 0 : pgwAddress.hashCode());
		result = prime * result + (int) (ratType ^ (ratType >>> 32));
		result = prime * result + ((stringRepr == null) ? 0 : stringRepr.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SgwCdrCategoryKey other = (SgwCdrCategoryKey) obj;
		if (chargingId != other.chargingId) {
			return false;
		}
		if (pgwAddress == null) {
			if (other.pgwAddress != null) {
				return false;
			}
		} else if (!pgwAddress.equals(other.pgwAddress)) {
			return false;
		}
		if (ratType != other.ratType){
			return false;
		}
		if (stringRepr == null) {
			if (other.stringRepr != null) {
				return false;
			}
		} else if (!stringRepr.equals(other.stringRepr)) {
			return false;
		}
		return true;
	}
	
	private String genStringRepresentation() {

		return String.format(pattern, chargingId, pgwAddress.getHostAddress(),ratType);
	}
	
	/**
	 * @return the chargingId
	 */
	public long getChargingId() {
		return chargingId;
	}

	/**
	 * @param chargingId
	 *            the chargingId to set
	 */
	public void setChargingId(long chargingId) {
		this.chargingId = chargingId;
	}

	/**
	 * @return the pgwAddress
	 */
	public InetAddress getPgwAddress() {
		return pgwAddress;
	}

	/**
	 * @param pgwAddress
	 *            the pgwAddress to set
	 */
	public void setPgwAddress(InetAddress pgwAddress) {
		this.pgwAddress = pgwAddress;
	}
	
	/**
	 * 
	 * @return ratType
	 */
	public int getRatType(){
		return ratType;
	}
	
	/**
	 * 
	 * @param ratType
	 */
	public void setRatType(int ratType){
		this.ratType = ratType;
	}
}
