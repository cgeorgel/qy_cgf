package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.net.InetAddress;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;

public class PgwCdrCategoryKey {

	private static String pattern = "P-GW CDRs(category key): {chargingID=%s, p-GWAddress=%s}";

	// TODO: chargingId值的获取, Number类型的精度问题, Number没有覆盖hashCode()以及equals()的问题...
//	private Number chargingId;
	private long chargingId;

	// 对接收到的实际相同的地址, 而表示形式(ipv4/ipv6)不同时, 如何处理?: 无须处理, 系统整体切换.
	private InetAddress pgwAddress;

	private String stringRepr;

	public PgwCdrCategoryKey() {
 
	}

	public PgwCdrCategoryKey(InputPgwCdrObject inputPgwCdr) {

		chargingId = inputPgwCdr.getChargingId();
		pgwAddress = inputPgwCdr.getPgwAddress();
	}

	@Override
	public String toString() {

		if (stringRepr != null) {
			return stringRepr;
		} else {
			return genStringRepresentation();
		}
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
		result = prime * result + ((stringRepr == null) ? 0 : stringRepr.hashCode());
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
		PgwCdrCategoryKey other = (PgwCdrCategoryKey) obj;
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

		return String.format(pattern, chargingId, pgwAddress.getHostAddress());
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
}
