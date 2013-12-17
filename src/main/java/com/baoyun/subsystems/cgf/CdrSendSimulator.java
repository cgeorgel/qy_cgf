package com.baoyun.subsystems.cgf;

import java.io.IOException;

import ch.panter.li.bi.asn.AsnException;
import ch.panter.li.bi.asn.AsnTag;
import ch.panter.li.bi.asn.AsnTagClass;
import ch.panter.li.bi.asn.AsnTagNature;
import ch.panter.li.bi.asn.model.AsnConstraintSize;
import ch.panter.li.bi.asn.model.AsnFieldImpl;
import ch.panter.li.bi.asn.model.AsnModuleImpl;
import ch.panter.li.bi.asn.model.AsnTypeImpl;
import ch.panter.li.bi.asn.model.AsnTypes;
import ch.panter.li.bi.asn.value.AsnBitString;
import ch.panter.li.bi.asn.value.AsnBoolean;
import ch.panter.li.bi.asn.value.AsnChoice;
import ch.panter.li.bi.asn.value.AsnEnumerated;
import ch.panter.li.bi.asn.value.AsnInteger;
import ch.panter.li.bi.asn.value.AsnOctetString;
import ch.panter.li.bi.asn.value.AsnSequence;
import ch.panter.li.bi.asn.value.AsnSequenceOf;
import ch.panter.li.bi.asn.value.AsnUTF8String;


import com.baoyun.subsystems.cgf.utils.BerCodingUtils;
import com.baoyun.subsystems.cgf.utils.PrintUtils;

/**
 * <B>话单模拟器 </B>
 * 
 * @author BaoXu
 * 
 */
public class CdrSendSimulator {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		setPartialPgwRecord(100000,new byte[]{ 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x04, 0x2b, 0x00, 0x00 },12,17,1,true,false);
		setPartialPgwRecord(100000,new byte[]{ 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x10, 0x2b, 0x00, 0x00 },8,0,2,false,true);
		
	}
	/**
	 * dura需要小于60s，方便测试
	 * @param recordOpenTime
	 * @param dura
	 * @param causeClosing
	 * @param seqNum
	 * @return
	 * @throws IOException
	 * @throws AsnException
	 */
	public static byte[] setPartialPgwRecord(int charId, byte[] recordOpenTime, int dura,int causeClosing,int seqNum,boolean start,boolean stop) throws IOException, AsnException{
		
		byte[] endTime = new byte[]{};
		endTime = recordOpenTime.clone();
		//简单判断，以后扩充
		if(recordOpenTime[5]+dura<60){
			
			endTime[5] = (byte) (recordOpenTime[5]+dura);
		}else{
			endTime[5] = (byte) (recordOpenTime[5]+dura-60);
			endTime[4] = (byte) (endTime[4]+1);
		}
		/**
		 *  recordType					[0] RecordType,
			servedIMSI					[3] IMSI,
			p-GWAddress					[4] GSNAddress,
			chargingID					[5] ChargingID,
			servingNodeAddress			[6] SEQUENCE OF GSNAddress,
			accessPointNameNI			[7] AccessPointNameNI OPTIONAL,
			pdpPDNType					[8] PDPType OPTIONAL,
			servedPDPPDNAddress			[9] PDPAddress OPTIONAL,
			dynamicAddressFlag			[11] DynamicAddressFlag OPTIONAL,
			listOfTrafficVolumes		[12] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
			recordOpeningTime			[13] TimeStamp,
			duration					[14] CallDuration,
			causeForRecClosing			[15] CauseForRecClosing,
		 */
		// recordType
		AsnInteger recordType = setAsnInteger(0,85);
		
		// servedIMSI
		AsnOctetString servedIMSI = setAsnOctetString(3, new byte[]{ 0x04, 0x24, 0x00, 0x00, 0x00, 0x11,0x11, (byte) 0xf1 });
		
		// p-GWAddress
		AsnChoice pGWAddress = setIpAddChoice(4, 0, new byte[] { 0x28, 0x28,0x28, 0x28 });

		// chargingID
		AsnInteger chargingID = setAsnInteger(5,charId);

		// servingNodeAddress
		AsnSequenceOf servingNodeAddress = setSquenceOfIpAdd(6,0,new byte[]{(byte) 0xc0,(byte) 0xa8, (byte) 0xca, 0x36});
	
		//DynamicAddressFlag
		AsnBoolean dynamicAddressFlag = setAsnBoolean(11,true);
		
		//recordOpeningTime
		//AsnOctetString recordOpeningTime = setAsnOctetString(13,new byte[]{ 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x04, 0x2b, 0x00, 0x00 });
		AsnOctetString recordOpeningTime = setAsnOctetString(13,recordOpenTime);

		//duration
		AsnInteger duration = setAsnInteger(14,dura);
		
		//causeForRecClosing
		/**
		 * 	normalRelease					(0),
			abnormalRelease					(4),
			cAMELInitCallRelease			(5),
			volumeLimit						(16),
			timeLimit						(17),
			servingNodeChange				(18),
			maxChangeCond					(19),
			managementIntervention			(20),
			intraSGSNIntersystemChange		(21),
			rATChange						(22),
			mSTimeZoneChange				(23),
			sGSNPLMNIDChange 				(24),
			unauthorizedRequestingNetwork	(52),
			unauthorizedLCSClient			(53),
			positionMethodFailure			(54),
			unknownOrUnreachableLCSClient	(58),
			listofDownstreamNodeChange		(59)
		 */
		AsnInteger causeForRecClosing = setAsnInteger(15,causeClosing);
		
		
		
		
		//chargingCharacteristics
		AsnOctetString chargingCharacteristics = setAsnOctetString(23,new byte[]{0x00, 0x01});
		
		//listOfServiceData
		//**************
		/**
		 * 	ratingGroup 					[1] RatingGroupId,
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
		 */
		AsnInteger ratingGroup = setAsnInteger(1,20);
		AsnInteger listLocalSequenceNumber = new AsnInteger();
		if(seqNum==-1){
			listLocalSequenceNumber = setAsnInteger(4,1);
		}else{
			listLocalSequenceNumber = setAsnInteger(4,seqNum);
		}
		
		AsnOctetString timeOfFirstUsage = setAsnOctetString(5,recordOpenTime);
		
		AsnOctetString timeOfLastUsage = setAsnOctetString(6,endTime);
		AsnInteger timeUsage = setAsnInteger(7,dura);
		//可能存在问题BitString的组装
		AsnBitString serviceConditionChange = setAsnBitString(8,new byte[]{0x04,0x00});
		
		//ePCQoSInformation
		AsnSequence ePCQoSInformation = new AsnSequence();
		AsnInteger qCI = setAsnInteger(1,6);
		AsnInteger guaranteedBitrateUL = setAsnInteger(4,0);
		AsnInteger guaranteedBitrateDL = setAsnInteger(5,0);
		AsnInteger aRP = setAsnInteger(6,1);
		ePCQoSInformation.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Constructed, 9));
		BerCodingUtils.addElement(ePCQoSInformation, qCI);
		BerCodingUtils.addElement(ePCQoSInformation, guaranteedBitrateUL);
		BerCodingUtils.addElement(ePCQoSInformation, guaranteedBitrateDL);
		BerCodingUtils.addElement(ePCQoSInformation, aRP);
		//listServingNodeAddress
		AsnChoice listServingNodeAddress = setIpAddChoice(10,0,new byte[]{(byte) 0xc0, (byte) 0xa8,
				(byte) 0xca, 0x36});
		//timeOfReport
		AsnOctetString timeOfReport = setAsnOctetString(14,endTime);
		
		
		AsnSequence changeOfServiceCondition = new AsnSequence();
		BerCodingUtils.addElement(changeOfServiceCondition, ratingGroup);
		BerCodingUtils.addElement(changeOfServiceCondition, listLocalSequenceNumber);
		BerCodingUtils.addElement(changeOfServiceCondition, timeOfFirstUsage);
		BerCodingUtils.addElement(changeOfServiceCondition, timeOfLastUsage);
		BerCodingUtils.addElement(changeOfServiceCondition, timeUsage);
		BerCodingUtils.addElement(changeOfServiceCondition, serviceConditionChange);
		BerCodingUtils.addElement(changeOfServiceCondition, ePCQoSInformation);
		BerCodingUtils.addElement(changeOfServiceCondition, listServingNodeAddress);
		BerCodingUtils.addElement(changeOfServiceCondition, timeOfReport);
		
		
		AsnSequenceOf listOfServiceData = new AsnSequenceOf();
		listOfServiceData.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Constructed, 34));
		BerCodingUtils.addElement(listOfServiceData, changeOfServiceCondition);
		//*******************************
		
		//servingNodeType
		AsnSequenceOf servingNodeType = setSequenceOfEnum(35,3);
		

		
		AsnSequence pgwRecord = new AsnSequence();
		pgwRecord.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Constructed, 79));
		BerCodingUtils.addElement(pgwRecord, recordType);
		BerCodingUtils.addElement(pgwRecord, servedIMSI);
		BerCodingUtils.addElement(pgwRecord, pGWAddress);
		BerCodingUtils.addElement(pgwRecord, chargingID);
		BerCodingUtils.addElement(pgwRecord, servingNodeAddress);
		BerCodingUtils.addElement(pgwRecord, dynamicAddressFlag);
		BerCodingUtils.addElement(pgwRecord, recordOpeningTime);
		BerCodingUtils.addElement(pgwRecord, duration);
		BerCodingUtils.addElement(pgwRecord, causeForRecClosing);
		
		if(seqNum != -1){
			//recordSequenceNumber
			AsnInteger recordSequenceNumber = setAsnInteger(17,seqNum);
		
			//localSequenceNumber
			AsnInteger localSequenceNumber = setAsnInteger(20,seqNum);
			BerCodingUtils.addElement(pgwRecord, recordSequenceNumber);
			BerCodingUtils.addElement(pgwRecord, localSequenceNumber);
		}
		
		BerCodingUtils.addElement(pgwRecord, chargingCharacteristics);
		BerCodingUtils.addElement(pgwRecord, listOfServiceData);
		BerCodingUtils.addElement(pgwRecord, servingNodeType);
		
		//startTime		
		if(start){
			AsnOctetString startTime = setAsnOctetString(38,recordOpenTime);
			BerCodingUtils.addElement(pgwRecord, startTime);
		}

		//stopTime
		if(stop){
			AsnOctetString stopTime = setAsnOctetString(39,endTime);
			BerCodingUtils.addElement(pgwRecord, stopTime);
		}
		
		
//		PrintUtils.printHex(BerCodingUtils.encode(pgwRecord));
		//PrintUtils.printHex(BerCodingUtils.encode(BerCodingUtils.decode(BerCodingUtils.encode(pgwRecord))));
		
		return BerCodingUtils.encode(pgwRecord);
	}
	
	public static AsnBitString setAsnBitString(int tagNum, byte[] content){
		AsnBitString asnBitString = new AsnBitString(content);
		asnBitString.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Primitive, tagNum));
		//asnBitString.setContent(content);
		return asnBitString;
		
	}
	public static AsnSequenceOf setSequenceOfEnum(int sequenceOfTagNum, int content){
		AsnSequenceOf sequenceOf = new AsnSequenceOf();
		sequenceOf.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Constructed, sequenceOfTagNum));
		AsnEnumerated eNum = new AsnEnumerated();
//		eNum.setTag(BerCodingUtils.createAsn1Tag(
//						AsnTagClass.ContextSpecific, AsnTagNature.Primitive,
//						contentTagNum));
		eNum.setValue(content);
		BerCodingUtils.addElement(sequenceOf, eNum);
		return sequenceOf;
	}
	public static AsnInteger setAsnInteger(int tagNum, long in){
		AsnInteger asnInteger = new AsnInteger();
		asnInteger.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Primitive, tagNum));
		asnInteger.setValue(in);
		return asnInteger;
	}
	
	public static AsnBoolean setAsnBoolean(int tagNum, Boolean in){
		AsnBoolean asnBoolean = new AsnBoolean();
		asnBoolean.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Primitive, tagNum));
		asnBoolean.setValue(in);
		return asnBoolean;
	}
	public static AsnOctetString setAsnOctetString(int tagNum, byte[] content){
		AsnOctetString asnOctetString = new AsnOctetString();
		asnOctetString.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Primitive, tagNum));
		asnOctetString.setContent(content);
		return asnOctetString;
	}
	public static AsnSequenceOf setSquenceOfIpAdd(int sequenceOfTagNum, int contentTagNum,  byte[] content) {
		AsnSequenceOf sequenceOf = new AsnSequenceOf();
		sequenceOf.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Constructed, sequenceOfTagNum));
		AsnOctetString ipAddress = new AsnOctetString();
		ipAddress
				.setTag(BerCodingUtils.createAsn1Tag(
						AsnTagClass.ContextSpecific, AsnTagNature.Primitive,
						contentTagNum));
		ipAddress.setContent(content);
		BerCodingUtils.addElement(sequenceOf, ipAddress);
		return sequenceOf;

	}
	
	public static AsnChoice setIpAddChoice(int choiceTagNum, int addTagNum,
			byte[] content) {
		AsnChoice choice = new AsnChoice();
		choice.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Constructed, choiceTagNum));
		AsnOctetString ipAddress = new AsnOctetString();
		ipAddress
				.setTag(BerCodingUtils.createAsn1Tag(
						AsnTagClass.ContextSpecific, AsnTagNature.Primitive,
						addTagNum));
		ipAddress.setContent(content);
		BerCodingUtils.addElement(choice, ipAddress);
		return choice;

	}

	@Deprecated
	public void testStructure() throws Exception {
		final AsnModuleImpl module = new AsnModuleImpl("TestModule");

		final AsnTag givenNameTag = AsnTypes.UTF8STRING.deriveApplicationTag(1);
		final AsnTag middleNameTag = AsnTypes.UTF8STRING
				.deriveApplicationTag(2);
		final AsnTag familyNameTag = AsnTypes.UTF8STRING
				.deriveApplicationTag(3);

		final AsnFieldImpl givenNameField = new AsnFieldImpl("given-name",
				givenNameTag, AsnTypes.UTF8STRING);
		final AsnFieldImpl middleNameField = new AsnFieldImpl("middle-name",
				middleNameTag, AsnTypes.UTF8STRING);
		final AsnFieldImpl familyNameField = new AsnFieldImpl("family-name",
				familyNameTag, AsnTypes.UTF8STRING);

		givenNameField.add(new AsnConstraintSize(1, 20));
		middleNameField.add(new AsnConstraintSize(1, 20));
		middleNameField.setOptional(true);
		familyNameField.add(new AsnConstraintSize(1, 20));

		final AsnTypeImpl nameType = new AsnTypeImpl(module, AsnTypes.SEQUENCE,
				"FullName", true);
		nameType.add(givenNameField);
		nameType.add(middleNameField);
		nameType.add(familyNameField);

		final AsnSequence name = new AsnSequence(nameType);
		// assertFalse( name.isValid() ); // empty

		final AsnUTF8String givenName = new AsnUTF8String();
		final AsnUTF8String middleName = new AsnUTF8String();
		final AsnUTF8String familyName = new AsnUTF8String();
		givenName.setTag(givenNameTag);
		middleName.setTag(middleNameTag);
		familyName.setTag(familyNameTag);

		name.getWritableItems().add(givenName);
		// assertFalse( name.isValid() ); // empty given-name, missing
		// family-name

		givenName.setValue("John");
		// assertFalse( name.isValid() ); // missing family-name

		name.getWritableItems().add(familyName);
		// assertFalse( name.isValid() ); // empty family-name

		familyName.setValue("Doe");
		// assertTrue( name.isValid() ); // fine

		name.getWritableItems().add(middleName);
		// assertFalse( name.isValid() ); // middle-name out-of-sequence & empty

		middleName.setValue("F.");
		// assertFalse( name.isValid() ); // middle-name out-of-sequence

		name.getWritableItems().remove(1);
		name.getWritableItems().add(familyName);
		// assertTrue( name.isValid() ); // fine

		middleName.setValue(null);
		// assertFalse( name.isValid() ); // empty middle-name

		name.getWritableItems().remove(1);
		// assertTrue( name.isValid() ); // fine

		final AsnTag wrongTag = AsnTypes.UTF8STRING.deriveApplicationTag(2);
		final AsnTypeImpl wrongType = new AsnTypeImpl(module,
				AsnTypes.UTF8STRING, "TestString", wrongTag);
		final AsnUTF8String wrongMiddleName = new AsnUTF8String(wrongType);
		wrongMiddleName.setValue("F.");

		name.getWritableItems().add(1, wrongMiddleName);
		// assertFalse( name.isValid() ); // middle-name of wrong type

	} // testStructure
}
