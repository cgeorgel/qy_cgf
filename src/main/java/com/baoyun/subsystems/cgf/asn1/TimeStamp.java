package com.baoyun.subsystems.cgf.asn1;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import com.baoyun.subsystems.cgf.utils.RepresentationDataUtils;

public class TimeStamp implements Cloneable, Comparable<TimeStamp> {

	private Calendar cal;

	private byte[] rawData;

	private boolean initialized = false;

	public TimeStamp(byte[] rawData) {

		if (rawData.length == 9) {
			this.rawData = rawData;
			init();
		}
	}

	private void init() {

		if (rawData != null) {

			// @formatter:off
			/*
			 *	TimeStamp ::= OCTET STRING (SIZE(9))
			 *		-- -- The contents of this field are a compact form of the UTCTime format
			 *		-- containing local time plus an offset to universal time. Binary coded
			 *		-- decimal encoding is employed for the digits to reduce the storage and
			 *		-- transmission overhead
			 *		-- e.g. YYMMDDhhmmssShhmm
			 *		-- where
			 *		-- YY = Year 00 to 99     BCD encoded
			 *		-- MM = Month 01 to 12    BCD encoded
			 *		-- DD = Day 01 to 31      BCD encoded
			 *		-- hh = hour 00 to 23     BCD encoded
			 *		-- mm = minute 00 to 59   BCD encoded
			 *		-- ss = second 00 to 59   BCD encoded
			 *		-- S  = Sign 0 = "+", "-" ASCII encoded
			 *		-- hh = hour 00 to 23     BCD encoded
			 *		-- mm = minute 00 to 59   BCD encoded
			 *		--
			 */
			// @formatter:on
			int raw_YY = RepresentationDataUtils.unsignedByte2Int(rawData[0]);
			int raw_MM = RepresentationDataUtils.unsignedByte2Int(rawData[1]);
			int raw_DD = RepresentationDataUtils.unsignedByte2Int(rawData[2]);
			int raw_hh = RepresentationDataUtils.unsignedByte2Int(rawData[3]);
			int raw_mm = RepresentationDataUtils.unsignedByte2Int(rawData[4]);
			int raw_ss = RepresentationDataUtils.unsignedByte2Int(rawData[5]);
			char raw_S = (char) RepresentationDataUtils.unsignedByte2Int(rawData[6]);
			int raw_hh_Z = RepresentationDataUtils.unsignedByte2Int(rawData[7]);
			int raw_mm_Z = RepresentationDataUtils.unsignedByte2Int(rawData[8]);

			Calendar cal = Calendar.getInstance();

			int currYear = cal.get(Calendar.YEAR);

			// FIXME: 先确认测试数据: {11, 1, 1, 2, 12, 4, 43, 0, 0}的含义, 再确认实现.
			int yearParam = judgeYearParam(currYear, raw_YY, raw_MM, raw_DD);

			String hh_Z_param;
			if (raw_hh_Z < 10) {
				hh_Z_param = "0" + Integer.toString(raw_hh_Z, 10);
			} else {
				hh_Z_param = Integer.toString(raw_hh_Z, 10);
			}

			String mm_Z_param;
			if (raw_mm_Z < 10) {
				mm_Z_param = "0" + Integer.toString(raw_mm_Z, 10);
			} else {
				mm_Z_param = Integer.toString(raw_mm_Z, 10);
			}

			TimeZone z = TimeZone.getTimeZone("GMT" + raw_S + hh_Z_param + ":" + mm_Z_param);

			cal.set(Calendar.YEAR, yearParam);
			cal.set(Calendar.MONTH, raw_MM - 1);
			cal.set(Calendar.DAY_OF_MONTH, raw_DD);
			cal.set(Calendar.HOUR_OF_DAY, raw_hh);
			cal.set(Calendar.MINUTE, raw_mm);
			cal.set(Calendar.SECOND, raw_ss);
			cal.setTimeZone(z);

			this.cal = cal;

			initialized = true;
		}
	}

	/**
	 * <p>
	 * 规范的实现.
	 * </p>
	 * <p>
	 * ref: Morgan_Kaufmann_publishers_asn.1_complete_John_Larmouth(1999).pdf:<br />
	 * 2.11 The two ASN.1 date/time types
	 * </p>
	 *
	 * @param currYear
	 * @param raw_YY
	 * @param raw_MM
	 * @param raw_DD
	 * @return
	 */
	private int judgeYearParam(int currYear, int raw_YY, int raw_MM, int raw_DD) {

		int yearParam;

		int currYearLow = getYearLow(currYear);
		if (raw_YY == currYearLow) {
			yearParam = raw_YY;
		} else if (raw_MM == (Calendar.DECEMBER + 1) && raw_DD == 31) {
			yearParam = currYear - 49 + raw_YY;
		} else {
			yearParam = currYear - 50 + raw_YY;
		}

		return yearParam;
	}

	/**
	 * <p>
	 * 以year: 2000开始.
	 * </p>
	 *
	 * @param currYear
	 * @param raw_YY
	 * @param raw_MM
	 * @param raw_DD
	 * @return
	 */
	@SuppressWarnings("unused")
	private int judgeYearParam2K(int currYear, int raw_YY, int raw_MM, int raw_DD) {

		return 2000 + raw_YY;
	}

	private int getYearLow(int year) {

		String yearString = Integer.toString(year, 10);
		int yearLow = Integer.parseInt(yearString.substring(yearString.length() - 2,
				yearString.length()), 10);

		return yearLow;
	}

	public long getTimeAsSecond() {

		if (initialized) {
			return cal.getTimeInMillis() / 1000;
		} else {
			throw new IllegalStateException("TimeStamp object: " + this + " NOT initialized!");
		}
	}

	@Override
	public String toString() {

		if (initialized) {
			return cal.getTime().toString();
		} else {
			throw new IllegalStateException("TimeStamp object: " + this + " NOT initialized!");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cal == null) ? 0 : cal.hashCode());
		result = prime * result + (initialized ? 1231 : 1237);
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
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
		TimeStamp other = (TimeStamp) obj;
		if (cal == null) {
			if (other.cal != null) {
				return false;
			}
		} else if (!cal.equals(other.cal)) {
			return false;
		}
		if (initialized != other.initialized) {
			return false;
		}
		return true;
	}

	@Override
	public TimeStamp clone() {

		if (isInitialized()) {
			TimeStamp cloned = new TimeStamp(getRawData());
			return cloned;
		} else {
			throw new IllegalStateException("TimeStamp object: " + this + " NOT initialized!");
		}
	}

	@Override
	public int compareTo(TimeStamp another) {

		if (!(this.initialized && another.initialized)) {
			throw new IllegalStateException("TimeStamp object NOT initialized! this: " + this
					+ ", thar: " + another);
		} else {

			return cal.compareTo(another.cal);
		}
	}

	/**
	 * @return the cal
	 */
	public Calendar getCal() {
		return (Calendar) cal.clone();
	}

	/**
	 * @return the rawData
	 */
	public byte[] getRawData() {
		return Arrays.copyOf(rawData, rawData.length);
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}
}
