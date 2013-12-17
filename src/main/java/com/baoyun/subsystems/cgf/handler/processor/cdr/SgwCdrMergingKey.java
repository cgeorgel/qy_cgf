package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.net.InetAddress;

import java.util.List;


import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;
import com.baoyun.subsystems.cgf.asn1.ServingNodeType;

public class SgwCdrMergingKey {
	private static String toStringPattern = "S-GW CDRs(merging key): {chargingID=%s, s-GWAddress=%s, p-GWAddress=%s, rATType=%s}";
	
	private long chargingId;

	private InetAddress sgwAddress;
	
	private InetAddress pgwAddress;
	
	private List<ServingNodeType> servingNodeTypes;

	private List<InetAddress> servingNodeAddresses;

	private int ratType;

	private String stringRepr;
	
	public SgwCdrMergingKey(){
		
	}
	
	public SgwCdrMergingKey(InputSgwCdrObject inputSgwCdr) {
		chargingId = inputSgwCdr.getChargingId();
		sgwAddress = inputSgwCdr.getSgwAddress();
		pgwAddress = inputSgwCdr.getPgwAddress();
		servingNodeAddresses = inputSgwCdr.getServingNodeAddresses();
		servingNodeTypes = inputSgwCdr.getServingNodeTypes();
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
	
	private String genStringRepresentation() {

		// pattern: "[" address-type "=" address ("," address-type "=" address)* "]"
		return String.format(toStringPattern, chargingId, sgwAddress.getHostAddress(), pgwAddress.getHostAddress(),
				(ratType == Integer.MIN_VALUE ? null : ratType));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (chargingId ^ (chargingId >>> 32));
		result = prime * result + ((sgwAddress == null) ? 0 : sgwAddress.hashCode());
		result = prime * result + ((pgwAddress == null) ? 0 : pgwAddress.hashCode());
		result = prime * result + ratType;
		result = prime * result
				+ ((servingNodeAddresses == null) ? 0 : servingNodeAddresses.hashCode());
		result = prime * result + ((servingNodeTypes == null) ? 0 : servingNodeTypes.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		SgwCdrMergingKey other = (SgwCdrMergingKey) obj;
		if (chargingId != other.chargingId) {
			return false;
		}
		if (sgwAddress == null) {
			if (other.sgwAddress != null) {
				return false;
			}
		} else if (!sgwAddress.equals(other.sgwAddress)) {
			return false;
		}
		if (pgwAddress == null) {
			if (other.pgwAddress != null) {
				return false;
			}
		} else if (!pgwAddress.equals(other.pgwAddress)) {
			return false;
		}
		if (ratType != other.ratType) {
			return false;
		}
	
		return true;
	}
	
	// getters/setters:

	/**
	 * 
	 * @return categoryKey
	 */
		public SgwCdrCategoryKey genSgwCdrCategoryKey() {

			SgwCdrCategoryKey categoryKey = new SgwCdrCategoryKey();
			categoryKey.setChargingId(chargingId);
			categoryKey.setPgwAddress(pgwAddress);
			categoryKey.setRatType(ratType);
			
			return categoryKey;
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
		 * @return the sgwAddress
		 */
		public InetAddress getSgwAddress() {
			return sgwAddress;
		}

		/**
		 * @param sgwAddress
		 *            the sgwAddress to set
		 */
		public void setSgwAddress(InetAddress sgwAddress) {
			this.sgwAddress = sgwAddress;
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
		 * @return the servingNodeTypes
		 */
		public List<ServingNodeType> getServingNodeTypes() {
			return servingNodeTypes;
		}

		/**
		 * @param servingNodeTypes
		 *            the servingNodeTypes to set
		 */
		public void setServingNodeTypes(List<ServingNodeType> servingNodeTypes) {
			this.servingNodeTypes = servingNodeTypes;
		}

		/**
		 * @return the servingNodeAddresss
		 */
		public List<InetAddress> getServingNodeAddresses() {
			return servingNodeAddresses;
		}

		/**
		 * @param servingNodeAddresss
		 *            the servingNodeAddresss to set
		 */
		public void setServingNodeAddresses(List<InetAddress> servingNodeAddresses) {
			this.servingNodeAddresses = servingNodeAddresses;
		}

		/**
		 * @return the ratType
		 */
		public int getRatType() {
			return ratType;
		}

		/**
		 * @param ratType
		 *            the ratType to set
		 */
		public void setRatType(int ratType) {
			this.ratType = ratType;
		}
}
