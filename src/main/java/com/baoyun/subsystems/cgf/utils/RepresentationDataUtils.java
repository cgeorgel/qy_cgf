package com.baoyun.subsystems.cgf.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * use big-endian(Java langurage, network transfer, ...).
 * </p>
 *
 * @author George
 *
 */
public class RepresentationDataUtils {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RepresentationDataUtils.class
			.getName());

	public enum Endianess {

		BE(true),
		LE(false);

		private boolean bigEndian;

		Endianess(boolean isBE) {

			bigEndian = isBE;
		}

		public boolean isBigEndian() {

			return bigEndian;
		}
	}

	public static int unsignedByte2Int(byte anUnsignedByte) {

		return 0x000000FF & anUnsignedByte;
	}

	public static int signedByte2Int(byte aSignedByte) {

		return aSignedByte;
	}

	public static byte int2UnsignedByte(int anInt) {

		if (anInt < 0 || anInt > 0xFF) {
			throw new IllegalArgumentException("can NOT convert from int: " + anInt
					+ " to unsigned byte: the range is too large!");
		}

		return (byte) (anInt & 0x000000FF);
	}

	public static byte int2SignedByte(int anInt) {

		if (anInt > Byte.MAX_VALUE || anInt < Byte.MIN_VALUE) {
			throw new IllegalArgumentException("converting from int: " + anInt
					+ " to byte will cause accuracy loss!");
		}

		return (byte) (anInt & 0x000000FF);
	}

	public static byte[] integralValue2ByteArray(long value, int len, Endianess... endianess) {

		if (len != 1 && len != 2 && len != 4 && len != 8) {
			throw new IllegalArgumentException(
					"only support integral value type: byte(8 bits), short(16 bits)" +
							", int(32 bits) and long(64 bits)");
		}

		boolean be = true;
		if (endianess.length != 0) {
			be = endianess[0].isBigEndian();
		}

		byte[] result = new byte[len];

		if (be) {
			for (int i = 0; i < len; ++i) {
				result[i] = (byte) ((value >> (8 * (len - i - 1))) & 0xFF);
			}
		} else {
			for (int i = 0; i < len; ++i) {
				result[i] = (byte) ((value >> (8 * i)) & 0xFF);
			}
		}

		return result;
	}

	public static byte[] short2ByteArray(short aShort) {

		return integralValue2ByteArray(aShort, 2);
	}

	public static byte[] int2ByteArray(int anInt) {

		return integralValue2ByteArray(anInt, 4);
	}

	public static byte[] long2ByteArray(long aLong) {

		return integralValue2ByteArray(aLong, 8);
	}

	private RepresentationDataUtils() {

	}
}
