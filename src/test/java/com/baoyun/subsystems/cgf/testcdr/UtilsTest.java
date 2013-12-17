/**
 *
 */
package com.baoyun.subsystems.cgf.testcdr;

import com.baoyun.subsystems.cgf.utils.BerCodingUtils;
import com.baoyun.subsystems.cgf.utils.RepresentationDataUtils;

/**
 * @author George
 *
 */
public class UtilsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		testConvert2ByteArray();
	}

	private static void testConvert2ByteArray() {

		short s = 1;
		byte[] bin = RepresentationDataUtils.short2ByteArray(s);

		System.out.println(BerCodingUtils.getReadableByteArray(bin, 10, " "));

		int i = 14;
		bin = RepresentationDataUtils.int2ByteArray(i);

		System.out.println(BerCodingUtils.getReadableByteArray(bin, 10, " "));

		long l = 23;
		bin = RepresentationDataUtils.long2ByteArray(l);

		System.out.println(BerCodingUtils.getReadableByteArray(bin, 10, " "));
	}
}
