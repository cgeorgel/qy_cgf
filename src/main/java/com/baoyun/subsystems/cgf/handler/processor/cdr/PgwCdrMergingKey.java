package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.asn1.ServingNodeType;

public class PgwCdrMergingKey {

	private static String toStringPattern = "P-GW CDRs(merging key): {chargingID=%s, p-GWAddress=%s, servingNodeAddress=%s, rATType=%s}";

	// TODO: chargingId, ratType值的获取, Number类型的精度问题, Number没有覆盖hashCode()以及equals()的问题...
//	private Number chargingId;
	private long chargingId;

	private InetAddress pgwAddress;

	private List<ServingNodeType> servingNodeTypes;

	private List<InetAddress> servingNodeAddresss;

	private int ratType;

	private String stringRepr;

	public PgwCdrMergingKey() {

	}

	public PgwCdrMergingKey(InputPgwCdrObject inputPgwCdr) {

		chargingId = inputPgwCdr.getChargingId();
		pgwAddress = inputPgwCdr.getPgwAddress();
		servingNodeAddresss = inputPgwCdr.getServingNodeAddresses();
		servingNodeTypes = inputPgwCdr.getServingNodeTypes();
		ratType = inputPgwCdr.getRatType();
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
		List<String> addresses = new ArrayList<String>();
		for (int i = 0; i < servingNodeTypes.size(); ++i) {
			addresses.add(servingNodeTypes.get(i).getStdLiteral() + "="
					+ servingNodeAddresss.get(i).getHostAddress());
		}
		return String.format(toStringPattern, chargingId, pgwAddress.getHostAddress(), addresses,
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
		result = prime * result + ((pgwAddress == null) ? 0 : pgwAddress.hashCode());
		result = prime * result + ratType;
		result = prime * result
				+ ((servingNodeAddresss == null) ? 0 : servingNodeAddresss.hashCode());
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
		PgwCdrMergingKey other = (PgwCdrMergingKey) obj;
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
		if (ratType != other.ratType) {
			return false;
		}
		// TODO: 对: servingNodeType和servingNodeAddress实际等价, 但顺序不同时的处理.
		if (servingNodeAddresss == null) {
			if (other.servingNodeAddresss != null) {
				return false;
			}
		} else if (!servingNodeAddresss.equals(other.servingNodeAddresss)) {
			return false;
		}
		if (servingNodeTypes == null) {
			if (other.servingNodeTypes != null) {
				return false;
			}
		} else if (!servingNodeTypes.equals(other.servingNodeTypes)) {
			return false;
		}
		return true;
	}

	// getters/setters:

	public PgwCdrCategoryKey genPgwCdrCategoryKey() {

		PgwCdrCategoryKey categoryKey = new PgwCdrCategoryKey();
		categoryKey.setChargingId(chargingId);
		categoryKey.setPgwAddress(pgwAddress);

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
	public List<InetAddress> getServingNodeAddresss() {
		return servingNodeAddresss;
	}

	/**
	 * @param servingNodeAddresss
	 *            the servingNodeAddresss to set
	 */
	public void setServingNodeAddresss(List<InetAddress> servingNodeAddresss) {
		this.servingNodeAddresss = servingNodeAddresss;
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
