package com.baoyun.subsystems.cgf.testcdr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.panter.li.bi.asn.AsnException;
import ch.panter.li.bi.asn.AsnTag;
import ch.panter.li.bi.asn.AsnTagClass;
import ch.panter.li.bi.asn.AsnTagImpl;
import ch.panter.li.bi.asn.AsnTagNature;
import ch.panter.li.bi.asn.AsnValue;
import ch.panter.li.bi.asn.model.AsnTypes;
import ch.panter.li.bi.asn.value.AsnBitString;
import ch.panter.li.bi.asn.value.AsnContainerValueBase;
import ch.panter.li.bi.asn.value.AsnEnumerated;
import ch.panter.li.bi.asn.value.AsnIA5String;
import ch.panter.li.bi.asn.value.AsnOctetString;
import ch.panter.li.bi.asn.value.AsnSequence;

import com.baoyun.subsystems.cgf.CdrSendSimulator;
import com.baoyun.subsystems.cgf.asn1.TimeStamp;
import com.baoyun.subsystems.cgf.utils.BerCodingUtils;
import com.baoyun.subsystems.cgf.utils.HandleFileUtils;
import com.baoyun.subsystems.cgf.utils.MergingUtils;
import com.baoyun.subsystems.cgf.utils.RepresentationDataUtils;

public class CdrTest {

	AsnValue pgwCdrObj;

	@Before
	public void setUp() throws IOException, AsnException {

		pgwCdrObj = BerCodingUtils.decode(makeByteArray());
	}

	@After
	public void tearDown() {

	}

	public static void main(String... args) throws Exception {

		byte[] binMsg = makeByteArray();

		@SuppressWarnings("unused")
		AsnValue obj;
		obj = BerCodingUtils.decode(binMsg);

//		System.out.println(MergingUtils.analysisInputPgwCdr(obj));

//		testReEncode(obj);

//		writeBinToFile(encodedAgain, "./benu_cdr_encodedAgain.bin");

//		testBerCodingUtils(binMsg);

//		testMiscUtils();

//		String file = "./bin_20120903.dat";

//		System.out.println(getListOfServiceData(obj));

//		AsnValue outputPgwCdr = testOutputPgwCdrGenerate(binMsg);

//		HandleFileUtils.saveFinalFile(BerCodingUtils.encode(outputPgwCdr));

//		testAsn1ValueCreation();

//		testDecodedFromOutputCdr(file);

//		testGeneratedMergingIdForInputPgwCdr(obj);

//		testIpAddressObjectToInetAddress(obj);

//		System.out.println(MergingUtils.analysisInputPgwCdr(obj));

//		byte[] timeStampRaw = new byte[] { 11, 1, 1, 2, 12, 4, 43, 0, 0 };
//		System.out.println(testTimeStamp(timeStampRaw));

//		System.out.println(testGetRecordOpeningTimeForInputPgwCdr(obj));

//		System.out.println(testGetPgwAddressValueOfInputCdr(obj).getHostAddress());

//		testCustomPgwCdr(1103);
	}

	@Test
	public void testReEncode() throws Exception {

		byte[] encodedAgain = BerCodingUtils.encode(pgwCdrObj);
		AsnValue decodedAgain = BerCodingUtils.decode(encodedAgain);
		System.out.println(MergingUtils.analysisInputPgwCdr(decodedAgain));

		decodedAgain = BerCodingUtils.decode(encodedAgain);

		System.out.println("decodedAgain.equals(obj)? " + decodedAgain.equals(pgwCdrObj));
	}

	public static byte[] makeByteArray() {

		byte[] pgwcdr = { (byte) 0xbf, 0x4f, (byte) 0x81, (byte) 0x9f, (byte) 0x80, 0x01, 0x55,
				(byte) 0x83,
				0x08, 0x04, 0x24, 0x00, 0x00, 0x00, 0x11, 0x11, (byte) 0xf1, (byte) 0xa4, 0x06,
				(byte) 0x80, 0x04,
				0x28, 0x28, 0x28, 0x28, (byte) 0x85, 0x01, 0x02, (byte) 0xa6, 0x06, (byte) 0x80,
				0x04, (byte) 0xc0,
				(byte) 0xa8, (byte) 0xca, 0x36, (byte) 0x8b, 0x01, (byte) 0xff, (byte) 0x8d, 0x09,
				0x0b, 0x01, 0x01,
				0x02, 0x0c, 0x04, 0x2b, 0x00, 0x00, (byte) 0x8e, 0x01, 0x14, (byte) 0x8f, 0x01,
				0x00, (byte) 0x97, 0x02,
				0x00, 0x01, (byte) 0xbf, 0x22, 0x46, 0x30, 0x44, (byte) 0x81, 0x01, 0x14,
				(byte) 0x84, 0x01, 0x01,
				(byte) 0x85, 0x09, 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x04, 0x2b, 0x00, 0x00,
				(byte) 0x86, 0x09, 0x0b, 0x01,
				0x01, 0x02, 0x0c, 0x18, 0x2b, 0x00, 0x00, (byte) 0x87, 0x01, 0x14, (byte) 0x88,
				0x02, 0x04, 0x00,
				(byte) 0xa9, 0x0c, (byte) 0x81, 0x01, 0x06, (byte) 0x84, 0x01, 0x00, (byte) 0x85,
				0x01, 0x00, (byte) 0x86,
				0x01, 0x01, (byte) 0xaa, 0x06, (byte) 0x80, 0x04, (byte) 0xc0, (byte) 0xa8,
				(byte) 0xca, 0x36,
				(byte) 0x8e, 0x09, 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x18, 0x2b, 0x00, 0x00,
				(byte) 0xbf, 0x23, 0x03, 0x0a,
				0x01, 0x03, (byte) 0x9f, 0x26, 0x09, 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x04, 0x2b,
				0x00, 0x00, (byte) 0x9f, 0x27,
				0x09, 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x18, 0x2b, 0x00, 0x00 };

		return pgwcdr;
	}

	@Deprecated
	private static List<?> getListOfServiceData(AsnValue obj) throws Exception {

		AsnTag tag = new AsnTagImpl(AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 34);

		AsnContainerValueBase container = (AsnContainerValueBase) obj;

		// [34] SEQUENCE OF ChangeOfServiceCondition OPTIONAL
		AsnContainerValueBase values = (AsnContainerValueBase) container.getItemByTag(tag);
//		System.out.println(values.size());

		List<AsnValue> changeOfServiceConditionList = new ArrayList<AsnValue>();

		for (AsnValue each : values.getItems()) {
			for (AsnValue deeperEach : ((AsnContainerValueBase) each).getItems()) {
				System.out.println("tag: " + deeperEach.getTag());
				System.out.println("type: " + deeperEach.getType());
				System.out.println("class: " + deeperEach.getClass());
				System.out.println();
			}

			AsnContainerValueBase eachChangeOfServiceCondition = (AsnContainerValueBase) each;

			// @formatter:off
			/*
				ChangeOfServiceCondition	::= SEQUENCE
				{
					--
					-- Used for Flow based Charging service data container
					--
					ratingGroup 					[1] RatingGroupId,
					chargingRuleBaseName			[2] ChargingRuleBaseName OPTIONAL,
					resultCode						[3] ResultCode OPTIONAL,
					localSequenceNumber				[4] LocalSequenceNumber OPTIONAL,
					timeOfFirstUsage				[5] TimeStamp OPTIONAL,
					timeOfLastUsage					[6] TimeStamp OPTIONAL,
					timeUsage 						[7] CallDuration OPTIONAL,
					serviceConditionChange			[8] ServiceConditionChange,
					qoSInformationNeg				[9] EPCQoSInformation OPTIONAL,
					servingNodeAddress 				[10] GSNAddress OPTIONAL,
					datavolumeFBCUplink				[12] DataVolumeGPRS OPTIONAL,
					datavolumeFBCDownlink			[13] DataVolumeGPRS OPTIONAL,
					timeOfReport					[14] TimeStamp,
					failureHandlingContinue			[16] FailureHandlingContinue OPTIONAL,
					serviceIdentifier				[17] ServiceIdentifier OPTIONAL,
					pSFurnishChargingInformation	[18] PSFurnishChargingInformation OPTIONAL,
					aFRecordInformation				[19] SEQUENCE OF AFRecordInformation OPTIONAL,
					userLocationInformation			[20] OCTET STRING OPTIONAL,
					eventBasedChargingInformation	[21] EventBasedChargingInformation OPTIONAL,
					timeQuotaMechanism				[22] TimeQuotaMechanism OPTIONAL,
					serviceSpecificInfo				[23] SEQUENCE OF ServiceSpecificInfo OPTIONAL
				}
			 */
			// @formatter:on
			AsnValue ratingGroup = BerCodingUtils.getAtomElementByTagNum(
					eachChangeOfServiceCondition, 1, AsnTypes.INTEGER);

			AsnValue localSequenceNumber = BerCodingUtils.getAtomElementByTagNum(
					eachChangeOfServiceCondition, 4, AsnTypes.INTEGER);

			AsnValue timeOfFirstUsage = BerCodingUtils.getAtomElementByTagNum(
					eachChangeOfServiceCondition, 5, AsnTypes.OCTET_STRING);

			AsnValue timeOfLastUsage = BerCodingUtils.getAtomElementByTagNum(
					eachChangeOfServiceCondition, 6, AsnTypes.OCTET_STRING);

			AsnValue timeUsage = BerCodingUtils.getAtomElementByTagNum(
					eachChangeOfServiceCondition, 7, AsnTypes.INTEGER);

			AsnValue serviceConditionChange = BerCodingUtils.getAtomElementByTagNum(
					eachChangeOfServiceCondition, 8, AsnTypes.BIT_STRING);
			if (serviceConditionChange instanceof AsnBitString) {
				System.out.println("serviceConditionChange.getType(): "
						+ serviceConditionChange.getType());
				AsnBitString bitStr = (AsnBitString) serviceConditionChange;
				System.out.println("serviceConditionChange.getBitCount(): " + bitStr.getBitCount());
				System.out.println("serviceConditionChange.getBits(): " + bitStr.getBits());
				System.out.print("serviceConditionChange content: ");
				for (byte eachByte : bitStr.getBits()) {
					System.out.print((0x00000011 & eachByte) + " ");
				}
			}
			System.out.println();

			AsnValue timeOfReport = BerCodingUtils.getAtomElementByTagNum(
					eachChangeOfServiceCondition, 14, AsnTypes.OCTET_STRING);

			AsnContainerValueBase coll = new AsnSequence();
			coll.getWritableItems().add(ratingGroup);
			coll.getWritableItems().add(localSequenceNumber);
			coll.getWritableItems().add(timeOfFirstUsage);
			coll.getWritableItems().add(timeOfLastUsage);
			coll.getWritableItems().add(timeUsage);
			coll.getWritableItems().add(serviceConditionChange);
			coll.getWritableItems().add(timeOfReport);

			changeOfServiceConditionList.add(coll);
		}

		return changeOfServiceConditionList;
	}

	@SuppressWarnings("unused")
	private static void writeBinToFile(byte[] bin, String file) throws Exception {

		OutputStream out = null;
		try {
			out = new FileOutputStream(file, false);
			out.write(bin);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private static byte[] readBinFromFile(String file) throws FileNotFoundException {

		byte[] buf = new byte[8192];
		InputStream in = new FileInputStream(file);

		while (true) {

		}
	}

	public static void testBerCodingUtils(byte[] bin) throws Exception {

		AsnContainerValueBase obj = (AsnContainerValueBase) BerCodingUtils.decode(bin);

//		testAsn1AtomValue(obj);

//		testAsn1ComplexValue(obj);
//
//		writeBinToFile(BerCodingUtils.encode(obj), "./encoded.bin");
//
//		AsnValue appended = testRecordAppend(obj);
//
//		writeBinToFile(BerCodingUtils.encode(appended), "./appended.bin");

		AsnValue moded = newCdrWithMoreServingNodeAddress(obj);

		testAsn1ComplexValue((AsnContainerValueBase) moded);

		byte[] modedBin = BerCodingUtils.encode(moded);

		AsnValue decodedFromModed = BerCodingUtils.decode(modedBin);

		testAsn1ComplexValue((AsnContainerValueBase) decodedFromModed);
	}

	public static void testMiscUtils() {

		int n = 127;
		System.out.println("n: " + n + ", casted: " + RepresentationDataUtils.int2SignedByte(n));

		n = -128;
		System.out.println("n: " + n + ", casted: " + RepresentationDataUtils.int2SignedByte(n));

		n = 129;
		System.out.println("n: " + n + ", casted: " + RepresentationDataUtils.int2UnsignedByte(n));

		n = 254;
		System.out.println("n: " + n + ", casted: " + RepresentationDataUtils.int2UnsignedByte(n));
	}

	public static void testAsn1AtomValue(AsnContainerValueBase container) throws Exception {

		System.out.println("outter-most tag: " + container.getTag());
		int selected = BerCodingUtils.judgeActualUntaggedChoiceItemTagNum(container);
		System.out.println("selected: " + selected);

		System.out.println("all tags of the outter-most object: "
				+ BerCodingUtils.listTags(container));

		AsnValue recordType = BerCodingUtils.getAtomElementByTagNum(container, 0, AsnTypes.INTEGER);
		System.out.println("recordType: " + recordType);

		AsnValue servedIMSI = BerCodingUtils.getAtomElementByTagNum(container, 3,
				AsnTypes.OCTET_STRING);
		System.out.println("servedIMSI: "
				+ BerCodingUtils.getReadableByteArray(((AsnOctetString) servedIMSI).getContent(),
						10, " "));

		AsnValue chargingId = BerCodingUtils.getAtomElementByTagNum(container, 5, AsnTypes.INTEGER);
		System.out.println("chargingId: " + chargingId);

		AsnValue dynamicAddressFlag = BerCodingUtils.getAtomElementByTagNum(container, 11,
				AsnTypes.BOOLEAN);
		System.out.println("dynamicAddressFlag: " + dynamicAddressFlag);

		AsnValue recordOpeningTime = BerCodingUtils.getAtomElementByTagNum(container, 13,
				AsnTypes.OCTET_STRING);
		System.out.println("recordOpeningTime: "
				+ BerCodingUtils.getReadableByteArray(((AsnOctetString) recordOpeningTime)
						.getContent(), 10, " "));

		AsnValue duration = BerCodingUtils.getAtomElementByTagNum(container, 14, AsnTypes.INTEGER);
		System.out.println("duration: " + duration);

		AsnValue causeForRecClosing = BerCodingUtils.getAtomElementByTagNum(container, 15,
				AsnTypes.INTEGER);
		System.out.println("causeForRecClosing: " + causeForRecClosing);

		AsnValue chargingCharacteristics = BerCodingUtils.getAtomElementByTagNum(container, 15,
				AsnTypes.OCTET_STRING);
		System.out.println("chargingCharacteristics: "
				+ BerCodingUtils.getReadableByteArray(((AsnOctetString) chargingCharacteristics)
						.getContent(), 10, " "));

		AsnValue startTime = BerCodingUtils.getAtomElementByTagNum(container, 15,
				AsnTypes.OCTET_STRING);
		System.out.println("startTime: "
				+ BerCodingUtils.getReadableByteArray(((AsnOctetString) startTime).getContent(),
						10, " "));

		AsnValue stopTime = BerCodingUtils.getAtomElementByTagNum(container, 15,
				AsnTypes.OCTET_STRING);
		System.out
				.println("stopTime: "
						+ BerCodingUtils.getReadableByteArray(
								((AsnOctetString) stopTime).getContent(), 10, " "));

		AsnContainerValueBase p_GWAddress = BerCodingUtils.getComplexElementByTagNum(container, 4);
		System.out.println(p_GWAddress);
		System.out.println("all tags of the p_GWAddress: " + BerCodingUtils.listTags(p_GWAddress));
	}

	public static void testAsn1ComplexValue(AsnContainerValueBase container) throws Exception {

		AsnValue servingNodeAddress = BerCodingUtils.getComplexElementByTagNum(container, 4);
		System.out.println(servingNodeAddress);
		System.out.println(((AsnContainerValueBase) servingNodeAddress).size());
		System.out.println(BerCodingUtils.listTags((AsnContainerValueBase) servingNodeAddress));

		List<AsnTag> choicesOfServingNodeAddress = new ArrayList<AsnTag>();
		for (AsnValue each : ((AsnContainerValueBase) servingNodeAddress).getItems()) {
			choicesOfServingNodeAddress.add(each.getTag());
		}

		System.out.println(choicesOfServingNodeAddress);

		List<AsnValue> values = new ArrayList<AsnValue>();
		for (AsnTag each : choicesOfServingNodeAddress) {

			int tagNum = each.getTagNumber().getValueAsInt();
			switch (tagNum) {
				case 0:
				case 1:
					values.add(BerCodingUtils.getAtomElementByTagNum(
							(AsnContainerValueBase) servingNodeAddress, tagNum,
							AsnTypes.OCTET_STRING));
					break;

				case 2:
				case 3:
					values.add(BerCodingUtils.getAtomElementByTagNum(
							(AsnContainerValueBase) servingNodeAddress, tagNum, AsnTypes.IA5STRING));
					break;

				default:
					break;
			}
		}

		for (AsnValue each : values) {
			if (each instanceof AsnIA5String) {
				System.out.println(each);
			} else if (each instanceof AsnOctetString) {
				System.out.println(BerCodingUtils.getReadableByteArray(((AsnOctetString) each)
						.getContent(), 10, " "));
			}
		}
	}

	public static void testAsn1ValueCreation() throws AsnException {

		AsnValue asn1Obj = BerCodingUtils.createAsn1Value(AsnTypes.SEQUENCE_OF);
		System.out.println(asn1Obj);
		System.out.println(asn1Obj.getClass());
		System.out.println(asn1Obj.getTag());
		System.out.println(asn1Obj.getType());
	}

	public static AsnContainerValueBase testRecordAppend(AsnContainerValueBase pgwRecord)
			throws AsnException {

		AsnEnumerated consolidationResult = (AsnEnumerated) BerCodingUtils
				.createAsn1Value(AsnTypes.ENUMERATED);
		consolidationResult.setValue(0);
		consolidationResult.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Primitive, 101));

		pgwRecord.getWritableItems().add(consolidationResult);

		return pgwRecord;
	}

	public static AsnValue newCdrWithMoreServingNodeAddress(AsnContainerValueBase container) {

		AsnValue servingNodeAddress = BerCodingUtils.getComplexElementByTagNum(container, 4);
		System.out.println("servingNodeAddress.toString(): " + servingNodeAddress);
		System.out.println("servingNodeAddress.size(): "
				+ ((AsnContainerValueBase) servingNodeAddress).size());
		System.out.println("all tags of servingNodeAddress: "
				+ BerCodingUtils.listTags((AsnContainerValueBase) servingNodeAddress));
		System.out.println("servingNodeAddress.getType(): " + servingNodeAddress.getType());

		AsnContainerValueBase addresses = (AsnContainerValueBase) servingNodeAddress;

		AsnValue cloneFrom = addresses.getItems().get(0);
		AsnValue cloned = cloneFrom.copyAsnValue();

		addresses.getWritableItems().add(cloned);

		System.out.println("servingNodeAddress.toString(): " + servingNodeAddress);
		System.out.println("servingNodeAddress.size(): "
				+ ((AsnContainerValueBase) servingNodeAddress).size());
		System.out.println("all tags of servingNodeAddress: "
				+ BerCodingUtils.listTags((AsnContainerValueBase) servingNodeAddress));

		System.out.println("servingNodeAddress.getItemByTag(0):f "
				+ addresses.getItemByTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
						AsnTagNature.Primitive, 0)));

		return container;
	}

	public static void testDecodedFromOutputCdr(String file) throws Exception {

		byte[] bin = HandleFileUtils.readFileByBytes(file);

		MergingUtils.analysisInputPgwCdr(BerCodingUtils.decode(bin));
	}

	public static AsnValue testOutputPgwCdrGenerate(byte[] binMsg) throws Exception {

		AsnValue inputCdr = BerCodingUtils.decode(binMsg);
		AsnValue outputCdr = MergingUtils.createOutputPgwCdrFromInputPgwCdr(
				(AsnContainerValueBase) inputCdr, 0);
		System.out.println(BerCodingUtils.getAtomElementByTagNum((AsnContainerValueBase) outputCdr,
				101, AsnTypes.ENUMERATED));
		System.out.println(BerCodingUtils.getComplexElementByTagNum(
				(AsnContainerValueBase) outputCdr, 20));

		System.out.println(MergingUtils.getPgwAddressOfInputCdr(outputCdr).equals(
				MergingUtils.getPgwAddressOfInputCdr(inputCdr)));

		System.out.println(MergingUtils.ipAddressObjectToString(MergingUtils
				.getPgwAddressOfInputCdr(inputCdr)));

		AsnContainerValueBase ls = BerCodingUtils.getComplexElementByTagNum(
				(AsnContainerValueBase) outputCdr, 20);
		AsnContainerValueBase taggedChoice = BerCodingUtils.getComplexElementByTagNum(ls, 0);
		System.out.println(BerCodingUtils.judgeActualTaggedChoiceItemTagNum(taggedChoice));
		System.out.println(BerCodingUtils.getElementByPosition(taggedChoice, 0).equals(
				MergingUtils.getPgwAddressOfInputCdr(outputCdr)));

		return outputCdr;
	}

	public static void testGeneratedMergingIdForInputPgwCdr(AsnValue inputPgwCdr) throws Exception {

		String genId = MergingUtils.genMergingIdForPgwCdr(inputPgwCdr);

		System.out.println(genId);
	}

	public static void testIpAddressObjectToInetAddress(AsnValue inputPgwCdr) throws AsnException,
			IOException {

		AsnValue ipAddressObj = BerCodingUtils.getTaggedChoiceElement(
				(AsnContainerValueBase) inputPgwCdr, 4);
		InetAddress address = MergingUtils.ipAddressObjectToInetAddress(ipAddressObj);

		System.out.println("IPAddress field: " + address.getCanonicalHostName());
	}

	public static Date testTimeStamp(byte[] rawData) {

		TimeStamp ts = new TimeStamp(rawData);
		return ts.getCal().getTime();
	}

	public static Date testGetRecordOpeningTimeForInputPgwCdr(AsnValue inputPgwCdr)
			throws Exception {

		TimeStamp ts = MergingUtils.getRecordOpeningTimeValueOfInputCdr(inputPgwCdr);
		return ts.getCal().getTime();
	}

	public static InetAddress testGetPgwAddressValueOfInputCdr(AsnValue inputCdr)
			throws AsnException, IOException {

		return MergingUtils.getPgwAddressValueOfInputCdr(inputCdr);
	}

	public static void testCustomPgwCdr(int charId) throws Exception {

		ArrayList<byte[]> array = new ArrayList<byte[]>();
		array.add(CdrSendSimulator.setPartialPgwRecord(charId, new byte[] { 0x0b, 0x01,
				0x01, 0x02, 0x0c, 0x04, 0x2b, 0x00, 0x00 }, 12, 17, 1, true, false));
		array.add(CdrSendSimulator.setPartialPgwRecord(charId, new byte[] { 0x0b, 0x01,
				0x01, 0x02, 0x0c, 0x10, 0x2b, 0x00, 0x00 }, 8, 0, 2, false, true));

		for (byte[] each : array) {
			AsnValue partialPgwCdr = BerCodingUtils.decode(each);
			System.out.println(MergingUtils.analysisInputPgwCdr(partialPgwCdr));
			System.out.println("***********************************************");
			System.out.println("***********************************************");
			System.out.println("***********************************************");
		}
	}
}
