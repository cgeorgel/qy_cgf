package com.baoyun.subsystems.cgf.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.iet.GtpPrimeInformationElement;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferRequest;
import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrType;

/**
 * Utilities for serialization/deserialization of CDRs.
 *
 *
 *
 */
public class CdrSerializationUtils {

//	public static PGWRecord deSerializeToInputPgwCdr(
//			GtpPrimeDataRecordTransferRequest cdr) throws Exception {
//
//		PGWRecord obj = new PGWRecord();
////		obj.decode(new BerInputStream(new ByteArrayInputStream(cdr
////				.toByteArray())));
//
//		return obj;
//	}

	/**
	 * 用于判断DataRecordTransferRequest里面携带的是PGW-CDR或者SGW-CDR
	 *
	 * @param cdr
	 * @return CdrType Pgw-CDR(85) or Sgw-CDR(84) value
	 */
	public static int judgeCDRTransRequestType(
			GtpPrimeDataRecordTransferRequest cdr) {

		GtpPrimeInformationElement receivedCDR = cdr
				.getInformationElement((byte) Constants.GTP_PRIME_IET_DATA_RECORD_PACKET);
		int result = CdrType.UNKNOWN;

		byte[] msgContent = receivedCDR.toByteArray();
		if (msgContent[9] == (byte) 0xbf && msgContent[10] == (byte) 0x4f) {
			return CdrType.PGW_CDR;
		}
		if (msgContent[9] == (byte) 0xbf && msgContent[10] == (byte) 0x4e) {
			return CdrType.SGW_CDR;
		} else {
			return result;
		}
	}

	/**
	 * 用于存储CDR时判断类型，从而改变文件后缀
	 *
	 * @param msgContent
	 *            输入为CDR的内容的字节数组
	 * @return CdrType Pgw-CDR(85) or Sgw-CDR(84) value
	 */
	public static int judgeCdrType(byte[] msgContent) {

		int result = CdrType.UNKNOWN;
		if (msgContent[0] == (byte) 0xbf && msgContent[1] == (byte) 0x4f) {
			return CdrType.PGW_CDR;
		}
		if (msgContent[0] == (byte) 0xbf && msgContent[1] == (byte) 0x4e) {
			return CdrType.SGW_CDR;
		} else {
			return result;
		}
	}

	/**
	 * 获取一个DataRecordTransferRequest消息里所有CDR的集合
	 *
	 * @param GtpPrimeDataRecordTransferRequest
	 * @return CDR内容的集合列表
	 */
	public static List<byte[]> getAllCdrInOneRequest(GtpPrimeDataRecordTransferRequest cdr) {

		List<byte[]> out = new ArrayList<byte[]>();
		GtpPrimeInformationElement receiveIET = cdr
				.getInformationElement((byte) Constants.GTP_PRIME_IET_DATA_RECORD_PACKET);
		byte[] wholeCDRPacket = receiveIET.toByteArray();

		int NumOfCDR = wholeCDRPacket[3] & 0xFF;
		int startIndex = 7;
		if (NumOfCDR == 0)
			return out;
		else {
			for (int i = 0; i < NumOfCDR; i++) {

				int length =((wholeCDRPacket[startIndex]<<8) & 0xFFFF) + convertByteToInt(wholeCDRPacket[startIndex+1]);
				byte[] arr =new byte[length];
				arr = Arrays.copyOfRange(wholeCDRPacket, startIndex+2, startIndex+2+length);
				out.add(arr);
				startIndex += length+2;
			}

			return out;
		}
	}

	private static int convertByteToInt(byte a) {
		return a & 0x00FF;
	}

	private CdrSerializationUtils() {

	}
}
