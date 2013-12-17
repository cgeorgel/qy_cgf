package com.baoyun.subsystems.cgf.utils;
/**
 * <ul>
 * <li>print byte[]{}'s content;</li>
 * <li></li>
 * </ul>
 *
 * @author xbao
 *
 */
public class PrintUtils {
	public static void printHex(byte[] coded) {
		System.out.println("to byte array in HEX : ");
		String hexDigits = "0123456789ABCDEF";
		for (int i = 0; i < coded.length; i++) {
			int c = coded[i];
			if (c < 0)
				c += 256;
			int hex1 = c & 0xF;
			int hex2 = c >> 4;
			System.out.print(hexDigits.substring(hex2, hex2 + 1));
			System.out.print(hexDigits.substring(hex1, hex1 + 1) + " ");
		}
		System.out.println();
	}

	public static void printArray(byte[] array) {
		// System.out.println("to byte array in DEC : ");
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
}
