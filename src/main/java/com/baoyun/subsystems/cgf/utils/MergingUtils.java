package com.baoyun.subsystems.cgf.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnException;
import ch.panter.li.bi.asn.AsnTagClass;
import ch.panter.li.bi.asn.AsnTagNature;
import ch.panter.li.bi.asn.AsnValue;
import ch.panter.li.bi.asn.AsnValueBase;
import ch.panter.li.bi.asn.model.AsnTypes;
import ch.panter.li.bi.asn.value.AsnBoolean;
import ch.panter.li.bi.asn.value.AsnChoice;
import ch.panter.li.bi.asn.value.AsnContainerValueBase;
import ch.panter.li.bi.asn.value.AsnEnumerated;
import ch.panter.li.bi.asn.value.AsnIA5String;
import ch.panter.li.bi.asn.value.AsnInteger;
import ch.panter.li.bi.asn.value.AsnOctetString;
import ch.panter.li.bi.util.ArgumentTool;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;
import com.baoyun.subsystems.cgf.asn1.ServingNodeType;
import com.baoyun.subsystems.cgf.asn1.TimeStamp;
import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrType;

/**
 * <ul>
 * <li>generate merging result stub {@link AsnValue} from CDR Objects;</li>
 * <li></li>
 * </ul>
 *
 * @author george
 *
 */
public class MergingUtils {

	private static final Logger _log = LoggerFactory.getLogger(MergingUtils.class.getName());

	public static int judgeInputCdrType(AsnValue inputCdr) throws AsnException {

		// sGWRecord [78] SGWRecord,
		// pGWRecord [79] PGWRecord
		int tagNum = BerCodingUtils.judgeActualUntaggedChoiceItemTagNum(inputCdr);
		switch (tagNum) {
			case 79:
				return CdrType.PGW_CDR;

			case 78:
				return CdrType.SGW_CDR;

			default:
				return CdrType.UNKNOWN;
		}
	}

	public static InputPgwCdrObject genInputPgwCdrObject(AsnValue inputPgwCdr) {

		return new InputPgwCdrObject(inputPgwCdr);
	}

	public static InputSgwCdrObject genInputSgwCdrObject(AsnValue inputPgwCdr) {

		return new InputSgwCdrObject(inputPgwCdr);
	}

	/**
	 * <p>
	 * 是否部分话单(S-GW/P-GW CDR均适用)?
	 * </p>
	 *
	 * @param pgwCdr
	 * @return
	 * @throws Exception
	 */
	public static boolean isPartialCdr(AsnValue inputCdr) throws Exception {

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
				// recordSequenceNumber [17] INTEGER OPTIONAL,
				AsnValue recordSequenceNumber = ((AsnContainerValueBase) inputCdr)
						.getItemByTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
								AsnTagNature.Primitive, 17));

				if (recordSequenceNumber != null) {
					return true;
				} else {
					return false;
				}

			case CdrType.SGW_CDR:
				// FIXME: to implement.
				throw new UnsupportedOperationException("S-GW CDR NOT supported yet!");

			default:
				throw new IllegalArgumentException("Unknown input CDR! " + inputCdr);
		}
	}

	/*
	 * TODO: isTriggerPgwCdrMerging(), isEndOfMergingPgwCdr()的实现, 可能还需结合CdrMergerContext容器,
	 * 以及合并容器等具体事件/状态跳转触发条件等信息.
	 */

	public static boolean isTriggerPgwCdrMerging(AsnValue inputPgwCdr) throws Exception {

		boolean result = false;

		int causeForRecClosing = getCauseForRecClosingValueOfInputCdr(inputPgwCdr).intValue();

		// @formatter:off
		/*
		 *	CauseForRecClosing ::= INTEGER {
		 *		-- -- In PGW-CDR and SGW-CDR the value servingNodeChange is used for partial record
		 *		-- generation due to Serving Node Address list Overflow
		 *		-- In SGSN servingNodeChange indicates the SGSN change
		 *		-- -- LCS related causes belong to the MAP error causes acc. TS 29.002 [60]
		 *		-- -- cause codes 0 to 15 are defined 'CauseForTerm' (cause for termination)
		 *		--
		 *		normalRelease (0),
		 *		abnormalRelease (4),
		 *		cAMELInitCallRelease (5),
		 *		volumeLimit (16),
		 *		timeLimit (17),
		 *		servingNodeChange (18),
		 *		maxChangeCond (19),
		 *		managementIntervention (20),
		 *		intraSGSNIntersystemChange (21),
		 *		rATChange (22),
		 *		mSTimeZoneChange (23),
		 *		sGSNPLMNIDChange (24),
		 *		unauthorizedRequestingNetwork (52),
		 *		unauthorizedLCSClient (53),
		 *		positionMethodFailure (54),
		 *		unknownOrUnreachableLCSClient (58),
		 *		listofDownstreamNodeChange (59)
		 *	}
		 */
		// @formatter:on
		switch (causeForRecClosing) {
			case 0:
			case 4:
			case 18:
			case 19:
			case 22:
				result = true;
				break;

			case 16:
			case 17:
				result = false;
				break;

			default:
				throw new IllegalArgumentException("unknown causeForRecClosing: "
						+ causeForRecClosing + ", input P-GW CDR: " + inputPgwCdr);
		}

		return result;
	}

	/**
	 * <p>
	 * 8.2.5 PGW-CDR的合并
	 * <p>
	 * 第四步：每次合并操作后检查Cause for Record Closing字段，如果Cause for Record
	 * Closing字段=normalRelease，说明本次会话过程结束，则完成合并过程，转第五步。如果Cause for Record Closing＝S-GW
	 * Change或maxChangeCond或者RAT
	 * Change，说明部分话单由于S-GW切换或者计费条件改变次数达到最大值（Qos改变或者费率时段变更引起）或者无线接入类型改变，则停止继续合并，转第五步。如果Cause for
	 * Record Closing字段原因是Partial record（如timelimit或volumelimit），说明还有后续记录，需要继续合并，转第二步。
	 * </p>
	 * </p>
	 *
	 * @param inputPgwCdr
	 * @return
	 * @throws Exception
	 */
	public static boolean isEndOfMergingPgwCdr(AsnValue inputPgwCdr) throws Exception {

		boolean result = false;

		int causeForRecClosing = getCauseForRecClosingValueOfInputCdr(inputPgwCdr).intValue();

		// @formatter:off
		/*
		 *	CauseForRecClosing ::= INTEGER {
		 *		-- -- In PGW-CDR and SGW-CDR the value servingNodeChange is used for partial record
		 *		-- generation due to Serving Node Address list Overflow
		 *		-- In SGSN servingNodeChange indicates the SGSN change
		 *		-- -- LCS related causes belong to the MAP error causes acc. TS 29.002 [60]
		 *		-- -- cause codes 0 to 15 are defined 'CauseForTerm' (cause for termination)
		 *		--
		 *		normalRelease (0),
		 *		abnormalRelease (4),
		 *		cAMELInitCallRelease (5),
		 *		volumeLimit (16),
		 *		timeLimit (17),
		 *		servingNodeChange (18),
		 *		maxChangeCond (19),
		 *		managementIntervention (20),
		 *		intraSGSNIntersystemChange (21),
		 *		rATChange (22),
		 *		mSTimeZoneChange (23),
		 *		sGSNPLMNIDChange (24),
		 *		unauthorizedRequestingNetwork (52),
		 *		unauthorizedLCSClient (53),
		 *		positionMethodFailure (54),
		 *		unknownOrUnreachableLCSClient (58),
		 *		listofDownstreamNodeChange (59)
		 *	}
		 */
		// @formatter:on
		switch (causeForRecClosing) {
			case 0:
			case 4:
			case 5:
				result = true;
				break;

			default:
				throw new IllegalArgumentException("unknown causeForRecClosing: "
						+ causeForRecClosing + ", input P-GW CDR: " + inputPgwCdr);
		}

		return result;
	}

	public static AsnValue getRecordTypeOfInputCdr(AsnValue inputCdr) throws Exception {

		AsnInteger recordType;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// recordType [0] RecordType,
				recordType = BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) inputCdr, 0, AsnTypes.INTEGER);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		return recordType;
	}

	public static Number getRecordTypeValueOfInputCdr(AsnValue inputCdr) throws Exception {

		AsnInteger recordType = (AsnInteger) getRecordTypeOfInputCdr(inputCdr);

		if (recordType == null) {
			return null;
		} else {
			return recordType.getValue();
		}
	}

	public static Number getChargingIdValueOfInputCdr(AsnValue inputCdr) throws Exception {

		AsnInteger chargingId;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// chargingID [5] ChargingID,
				chargingId = BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) inputCdr, 5, AsnTypes.INTEGER);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (chargingId == null) {
			return null;
		} else {
			return chargingId.getValue();
		}
	}

	public static AsnValue getPgwAddressOfInputCdr(AsnValue inputCdr) throws Exception {

		AsnValue pgwAddress;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {

			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// s-GWAddress [4] GSNAddress,
				pgwAddress = BerCodingUtils.getTaggedChoiceElement(
						(AsnContainerValueBase) inputCdr, 4);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		return pgwAddress;
	}

	public static AsnValue getSgwAddressOfInputCdr(AsnValue inputCdr) throws Exception {

		AsnValue sgwAddress;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {

			case CdrType.SGW_CDR:
				// s-GWAddress [4] GSNAddress,
				sgwAddress = BerCodingUtils.getTaggedChoiceElement(
						(AsnContainerValueBase) inputCdr, 4);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		return sgwAddress;
	}

	public static InetAddress getPgwAddressValueOfInputCdr(AsnValue inputCdr) throws AsnException,
			IOException {

		AsnValue pgwAddress;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {

			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// s-GWAddress [4] GSNAddress,
				pgwAddress = BerCodingUtils.getTaggedChoiceElement(
						(AsnContainerValueBase) inputCdr, 4);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (pgwAddress == null) {
			return null;
		} else {
			return ipAddressObjectToInetAddress(pgwAddress);
		}
	}

	public static AsnValue getServingNodeAddressesOfInputCdr(AsnValue inputCdr) throws AsnException {

		AsnValue servingNodeAddresses;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {

			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// servingNodeAddress [6] SEQUENCE OF GSNAddress,
				servingNodeAddresses = BerCodingUtils.getComplexElementByTagNum(
						(AsnContainerValueBase) inputCdr, 6);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		return servingNodeAddresses;
	}

	public static List<InetAddress> getServingNodeAddressesValueOfInputCdr(AsnValue inputCdr)
			throws AsnException, IOException {

		AsnContainerValueBase servingNodeAddressesObj;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {

			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// servingNodeAddress [6] SEQUENCE OF GSNAddress,
				servingNodeAddressesObj = BerCodingUtils.getComplexElementByTagNum(
						(AsnContainerValueBase) inputCdr, 6);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (servingNodeAddressesObj == null) {

			return null;
		} else {

			List<InetAddress> servingNodeAddresses = new ArrayList<InetAddress>();
			for (int i = 0; i < servingNodeAddressesObj.size(); ++i) {
				servingNodeAddresses.add(ipAddressObjectToInetAddress(servingNodeAddressesObj
						.getItems().get(i)));
			}

			return servingNodeAddresses;
		}
	}

	/**
	 * <p>
	 * PgwRecord: 根据接收的CDR, 创建将作为输出的CDR:
	 * <ul>
	 * <li><b>添加</b>项: consolidationResult, tag number: 101;</li><br />
	 * <br />
	 * <li><b>修改</b>项: recordSequenceNumber, tag number: 17:
	 * 改为包含所有部分话单recordSequenceNumber的SEQUENCE;</li><br />
	 * <br />
	 * <li><b>修改</b>项: localSequenceNumber, tag number: 21: 改为包含所有部分话单localSequenceNumber的SEQUENCE
	 * OF.</li>
	 * </ul>
	 * </p>
	 *
	 * TODO: [101]的编码顺序问题?
	 *
	 * @return
	 * @throws Exception
	 */
	public static AsnContainerValueBase createOutputPgwCdrFromInputPgwCdr(
			AsnContainerValueBase inputCdr,
			int consolidationResultInitValue) throws Exception {

		AsnContainerValueBase outputCdr = (AsnContainerValueBase) inputCdr.copyAsnValue();

		// @formatter:off
		// [101] consolidationResult:
		/*
		 *	ConsolidationResult::= ENUMERATED {
		 *		normal                    (0),
		 *		abnormal                  (1),
		 *		forInterSGSNConsolidation (2),
		 *		reachLimit                (3),
		 *		onlyOneCDRGenerated       (4)
		 *	}
		 */
		// @formatter:on
		AsnEnumerated consolidationResult = (AsnEnumerated) BerCodingUtils
				.createAsn1Value(AsnTypes.ENUMERATED);

		consolidationResult.setValue(consolidationResultInitValue);
		consolidationResult.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Primitive, 101));

		BerCodingUtils.addElement(outputCdr, consolidationResult);

		// [17] recordSequenceNumber
		AsnValue oriRecordSequenceNumber = inputCdr.getItemByTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific,
				AsnTagNature.Primitive, 17));

		AsnValue cDFAddress = getPgwAddressOfInputCdr(inputCdr).copyAsnValue();

		if (isPartialCdr(inputCdr)) {
			// @formatter:off
			/*
			 *	RECORDSequenceNumber ::= SEQUENCE {
			 *		cDFAddress       [0] EXPLICIT CDFAddress,
			 *		recordNumberList [1] RecordNumberList
			 *	}
			 */
			// @formatter:on
			// 整个recordSequenceNumber对象:
			AsnContainerValueBase listOfRecordSequenceNumber = (AsnContainerValueBase) BerCodingUtils
					.createAsn1Value(AsnTypes.SEQUENCE);
			listOfRecordSequenceNumber.setTag(BerCodingUtils.createAsn1Tag(
					AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 17));

			// recordSequenceNumber中的第一项: cDFAddress
			AsnValueBase choiceOfCDFAddress = new AsnChoice();
			((AsnContainerValueBase) choiceOfCDFAddress).getWritableItems().add(cDFAddress);
			choiceOfCDFAddress.setTag(BerCodingUtils.createAsn1Tag(
					AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 0));

			BerCodingUtils.addElement(listOfRecordSequenceNumber, choiceOfCDFAddress);

			// recordSequenceNumber中的第二项: recordNumberList
			AsnValueBase recordNumberList = BerCodingUtils.createAsn1Value(AsnTypes.SEQUENCE_OF);
			((AsnContainerValueBase) recordNumberList).getWritableItems().add(
					oriRecordSequenceNumber.copyAsnValue());
			recordNumberList.setTag(BerCodingUtils.createAsn1Tag(
					AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 1));

			BerCodingUtils.addElement(listOfRecordSequenceNumber, recordNumberList);

			// 替换[17]为列表:
			BerCodingUtils.replaceElementByTagNum(outputCdr, listOfRecordSequenceNumber, 17);
		}

		// localSequenceNumber [20] LOCALRECORDSequenceNumber OPTIONAL,
		AsnValue orilocalSequenceNumber = inputCdr.getItemByTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific,
				AsnTagNature.Primitive, 20));

		// @formatter:off
		/*
		 *	LocalRecordNumberList ::= SEQUENCE {
		 *		cDFAddress       [0] EXPLICIT CDFAddress,
		 *		recordNumberList [1] RecordNumberList
		 *	}
		 */
		// @formatter:on

		// TODO: 当前实现: 对于output CDR中的localSequenceNumber项([20]), 无论input
		// CDR是否包含localSequenceNumber项, 都创建1个列表.

		// 整个localSequenceNumber对象:
		AsnContainerValueBase listOfLocalSequenceNumber = (AsnContainerValueBase) BerCodingUtils
				.createAsn1Value(AsnTypes.SEQUENCE);
		listOfLocalSequenceNumber.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 20));

		// localSequenceNumber中的第一项: cDFAddress
		AsnValueBase choiceOfCDFAddress = new AsnChoice();
		((AsnContainerValueBase) choiceOfCDFAddress).getWritableItems().add(cDFAddress);
		choiceOfCDFAddress.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 0));

		BerCodingUtils.addElement(listOfLocalSequenceNumber, choiceOfCDFAddress);

		// localSequenceNumber中的第二项: localrecordNumberList
		AsnValueBase recordNumberList = BerCodingUtils.createAsn1Value(AsnTypes.SEQUENCE_OF);

		if (orilocalSequenceNumber != null) {
			// FIXME: temp debug: 20130709
//			_log.info("recordNumberList: " + recordNumberList);
//			if (recordNumberList != null) {
//				_log.info("recordNumberList.getWritableItems(): " + ((AsnContainerValueBase) recordNumberList).getWritableItems());
//			}
			if (oriRecordSequenceNumber != null) {
				((AsnContainerValueBase) recordNumberList).getWritableItems().add(
					oriRecordSequenceNumber.copyAsnValue());
			}
		}
		recordNumberList.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 1));

		BerCodingUtils.addElement(listOfLocalSequenceNumber, recordNumberList);

		if (orilocalSequenceNumber != null) {
			// 替换[20]为列表:
			BerCodingUtils.replaceElementByTagNum(outputCdr, listOfLocalSequenceNumber, 20);
		} else {
			BerCodingUtils.addElement(outputCdr, listOfLocalSequenceNumber);
		}

		return outputCdr;
	}

	public static AsnContainerValueBase createOutputSgwCdrFromInputSgwCdr(
			AsnContainerValueBase inputCdr,
			int consolidationResultInitValue) throws Exception {

		AsnContainerValueBase outputCdr = (AsnContainerValueBase) inputCdr.copyAsnValue();

		// @formatter:off
		// [101] consolidationResult:
		/*
		 *	ConsolidationResult::= ENUMERATED {
		 *		normal                    (0),
		 *		abnormal                  (1),
		 *		forInterSGSNConsolidation (2),
		 *		reachLimit                (3),
		 *		onlyOneCDRGenerated       (4)
		 *	}
		 */
		// @formatter:on
		AsnEnumerated consolidationResult = (AsnEnumerated) BerCodingUtils
				.createAsn1Value(AsnTypes.ENUMERATED);

		consolidationResult.setValue(consolidationResultInitValue);
		consolidationResult.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Primitive, 101));

		BerCodingUtils.addElement(outputCdr, consolidationResult);

		// [17] recordSequenceNumber
		AsnValue oriRecordSequenceNumber = inputCdr.getItemByTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific,
				AsnTagNature.Primitive, 17));

		AsnValue cDFAddress = getSgwAddressOfInputCdr(inputCdr).copyAsnValue();

		if (isPartialCdr(inputCdr)) {
			// @formatter:off
			/*
			 *	RECORDSequenceNumber ::= SEQUENCE {
			 *		cDFAddress       [0] EXPLICIT CDFAddress,
			 *		recordNumberList [1] RecordNumberList
			 *	}
			 */
			// @formatter:on
			// 整个recordSequenceNumber对象:
			AsnContainerValueBase listOfRecordSequenceNumber = (AsnContainerValueBase) BerCodingUtils
					.createAsn1Value(AsnTypes.SEQUENCE);
			listOfRecordSequenceNumber.setTag(BerCodingUtils.createAsn1Tag(
					AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 17));

			// recordSequenceNumber中的第一项: cDFAddress
			AsnValueBase choiceOfCDFAddress = new AsnChoice();
			((AsnContainerValueBase) choiceOfCDFAddress).getWritableItems().add(cDFAddress);
			choiceOfCDFAddress.setTag(BerCodingUtils.createAsn1Tag(
					AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 0));

			BerCodingUtils.addElement(listOfRecordSequenceNumber, choiceOfCDFAddress);

			// recordSequenceNumber中的第二项: recordNumberList
			AsnValueBase recordNumberList = BerCodingUtils.createAsn1Value(AsnTypes.SEQUENCE_OF);
			((AsnContainerValueBase) recordNumberList).getWritableItems().add(
					oriRecordSequenceNumber.copyAsnValue());
			recordNumberList.setTag(BerCodingUtils.createAsn1Tag(
					AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 1));

			BerCodingUtils.addElement(listOfRecordSequenceNumber, recordNumberList);

			// 替换[17]为列表:
			BerCodingUtils.replaceElementByTagNum(outputCdr, listOfRecordSequenceNumber, 17);
		}

		// localSequenceNumber [20] LOCALRECORDSequenceNumber OPTIONAL,
		AsnValue orilocalSequenceNumber = inputCdr.getItemByTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific,
				AsnTagNature.Primitive, 20));

		// @formatter:off
		/*
		 *	LocalRecordNumberList ::= SEQUENCE {
		 *		cDFAddress       [0] EXPLICIT CDFAddress,
		 *		recordNumberList [1] RecordNumberList
		 *	}
		 */
		// @formatter:on

		// TODO: 当前实现: 对于output CDR中的localSequenceNumber项([20]), 无论input
		// CDR是否包含localSequenceNumber项, 都创建1个列表.

		// 整个localSequenceNumber对象:
		AsnContainerValueBase listOfLocalSequenceNumber = (AsnContainerValueBase) BerCodingUtils
				.createAsn1Value(AsnTypes.SEQUENCE);
		listOfLocalSequenceNumber.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 20));

		// localSequenceNumber中的第一项: cDFAddress
		AsnValueBase choiceOfCDFAddress = new AsnChoice();
		((AsnContainerValueBase) choiceOfCDFAddress).getWritableItems().add(cDFAddress);
		choiceOfCDFAddress.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 0));

		BerCodingUtils.addElement(listOfLocalSequenceNumber, choiceOfCDFAddress);

		// localSequenceNumber中的第二项: localrecordNumberList
		AsnValueBase recordNumberList = BerCodingUtils.createAsn1Value(AsnTypes.SEQUENCE_OF);
		if (orilocalSequenceNumber != null) {
			((AsnContainerValueBase) recordNumberList).getWritableItems().add(
					oriRecordSequenceNumber.copyAsnValue());
		}
		recordNumberList.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 1));

		BerCodingUtils.addElement(listOfLocalSequenceNumber, recordNumberList);

		if (orilocalSequenceNumber != null) {
			// 替换[20]为列表:
			BerCodingUtils.replaceElementByTagNum(outputCdr, listOfLocalSequenceNumber, 20);
		} else {
			BerCodingUtils.addElement(outputCdr, listOfLocalSequenceNumber);
		}

		return outputCdr;
	}

	/**
	 * 重构到{@link com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrCategoryKey
	 * PgwCdrCategoryKey}, {@link com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingKey
	 * PgwCdrMergingKey}后, 已不再使用.
	 *
	 * @param pgwCdr
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public static String genMergingIdForPgwCdr(AsnValue pgwCdr) throws Exception {

		AsnContainerValueBase inputPgwCdr = (AsnContainerValueBase) pgwCdr;
		// @formatter:off
		/*
		 *	8.2.5 PGW-CDR的合并
		 *	1)合并依据
		 *		记录中的P-GW地址+S-GW地址+C-ID+ RAT Type及记录类型（PGW-CDR记录类型）是合并的依据
		 *	...
		 *	7)合并流程
		 *		第一步：根据P-GW地址+ S-GW地址+C-ID+RAT Type将原始PGW-CDR话单分类
		 *		第二步：话单合并，CDR合并过程中首先看记录的Record Sequence Number是否在
		 *	Record Sequence Number列表中存在，如果存在则说明是重复话单，予以剔除，回到第二步
		 *	否则将该字段加入列表，进入第三步。
		 *
		 *	PGWRecord 	::= SET
		 *	{
		 *		...
		 *		p-GWAddress						[4] GSNAddress,
		 *		chargingID						[5] ChargingID,
		 *		servingNodeAddress				[6] SEQUENCE OF GSNAddress,
		 *		...
		 *		recordSequenceNumber			[17] INTEGER OPTIONAL,
		 *		...
		 *		rATType							[30] RATType OPTIONAL,
		 *		...
		 *		servingNodeType					[35] SEQUENCE OF ServingNodeType,
		 *		...
		 *	}
		 */
		// @formatter:on
		AsnValue p_GWAddress = getPgwAddressOfInputCdr(inputPgwCdr);
		Number chargingID = getChargingIdValueOfInputCdr(inputPgwCdr);
		Number ratType = getRatTypeValueOfInputCdr(inputPgwCdr);
		Number recordSequenceNumber = getRecordSequenceNumberValueOfInputCdr(inputPgwCdr);

		// pattern: servingNodeTypeName "=" addressString
		List<String> servingNodeAddressStrings = new ArrayList<String>();
		List<InetAddress> servingNodeAddresses = getServingNodeAddressesValueOfInputCdr(inputPgwCdr);
		List<ServingNodeType> servingNodeTypes = getServingNodeTypesValueOfInputCdr(inputPgwCdr);

		for (int i = 0; i < servingNodeTypes.size(); ++i) {
			servingNodeAddressStrings.add(servingNodeTypes.get(i).getStdLiteral() + "="
					+ servingNodeAddresses.get(i).getHostAddress());
		}

		String pattern = "PgwCdr: {chargingID=%s, p-GWAddress=%s, servingNodeAddress=%s, rATType=%s, recordSequenceNumber=%s}";

		return String.format(
				pattern,
				chargingID,
				ipAddressObjectToString(p_GWAddress),
				servingNodeAddressStrings,
				ratType,
				recordSequenceNumber);
	}

	/**
	 * 应该仅供调试, 日志试用, 内部交互请使用{@link #resolveServingNodeTypes(AsnValue)},
	 * {@link #servingNodeTypeObjectToServingNodeType(AsnValue)};<br />
	 * 或者直接使用{@link #getServingNodeTypesValueOfInputCdr(AsnValue)}.
	 *
	 * @param servingNodeTypesObj
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	@Deprecated
	public static List<String> resolveServingNodeTypesToStrings(AsnValue servingNodeTypesObj)
			throws AsnException, IOException {

		ArgumentTool.nonNullArgument(servingNodeTypesObj);

		List<String> typeNames = new ArrayList<String>();

		int size = ((AsnContainerValueBase) servingNodeTypesObj).size();
		for (int i = 0; i < size; ++i) {

			AsnValue eachServingNodeType = ((AsnContainerValueBase) servingNodeTypesObj).getItems()
					.get(i);
			AsnEnumerated eachServingNodeActualType = BerCodingUtils.interpretAsAsn1Atom(
					eachServingNodeType, AsnTypes.ENUMERATED);

			int typeAsInt = eachServingNodeActualType.getValueAsInt();
			String servingNodeAddressName;
			switch (typeAsInt) {
				case 0:
					servingNodeAddressName = "sGSN(" + typeAsInt + ")";
					break;

				case 1:
					servingNodeAddressName = "pMIPSGW(" + typeAsInt + ")";
					break;

				case 2:
					servingNodeAddressName = "gTPSGW(" + typeAsInt + ")";
					break;

				case 3:
					servingNodeAddressName = "ePDG(" + typeAsInt + ")";
					break;

				case 4:
					servingNodeAddressName = "hSGW(" + typeAsInt + ")";
					break;

				case 5:
					servingNodeAddressName = "mME(" + typeAsInt + ")";
					break;

				default:
					servingNodeAddressName = "unknown ServingNodeType (value: " + typeAsInt + ")";
					// log: unknown ServingNodeType
					break;
			}
			typeNames.add(servingNodeAddressName);
		}

		return typeNames;
	}

	/**
	 * 应该仅供调试, 日志试用, 内部交互请使用{@link #resolveServingNodeTypesToStrings(AsnValue)}.
	 *
	 * @param servingNodeTypesObj
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	@Deprecated
	public static String servingNodeTypesToString(AsnValue servingNodeTypesObj)
			throws AsnException, IOException {

		StringBuilder sb = new StringBuilder("[");

		for (String each : resolveServingNodeTypesToStrings(servingNodeTypesObj)) {
			sb.append(each).append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());

		return sb.append("]").toString();
	}

	/**
	 * 应该仅供调试, 日志试用, 内部交互请使用{@link #resolveServingNodeAddresses(AsnValue)}, 以及
	 * {@link #ipAddressObjectToInetAddress(AsnValue)}
	 *
	 * @param servingNodeAddressesObj
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	@Deprecated
	public static String servingNodeAddressesToString(AsnValue servingNodeAddressesObj)
			throws AsnException, IOException {

		StringBuilder sb = new StringBuilder("[");

		for (AsnValue each : resolveServingNodeAddresses(servingNodeAddressesObj)) {
			String eachAddressToString = ipAddressObjectToString(each);
			sb.append(eachAddressToString).append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());

		return sb.append("]").toString();
	}

	/**
	 * 应该仅供调试, 日志试用, 内部交互请使用{@link #ipAddressObjectToInetAddress(AsnValue)}.
	 *
	 * @param addressObj
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	@Deprecated
	public static String ipAddressObjectToString(AsnValue addressObj) throws AsnException,
			IOException {

		String addressString;

		int selectedItemNum = BerCodingUtils.judgeActualUntaggedChoiceItemTagNum(addressObj);
		switch (selectedItemNum) {
			case 0:
			case 1:
				addressObj = BerCodingUtils.interpretAsAsn1Atom(addressObj, AsnTypes.OCTET_STRING);
				addressString = BerCodingUtils.getReadableByteArray(
						((AsnOctetString) addressObj).getContent(), 10, ".");
				break;

			case 2:
			case 3:
				addressObj = BerCodingUtils.interpretAsAsn1Atom(addressObj, AsnTypes.IA5STRING);
				addressString = ((AsnIA5String) addressObj).toString();
				break;

			default:
				addressString = "Unknown IPAddress selection: " + addressObj;
				// log: unknown IPAddress selection
				break;
		}

		return addressString;
	}

	public static List<AsnValue> resolveServingNodeAddresses(AsnValue servingNodeAddressesObj) {

		ArgumentTool.nonNullArgument(servingNodeAddressesObj);

		List<AsnValue> addresses = new ArrayList<AsnValue>();

		int size = ((AsnContainerValueBase) servingNodeAddressesObj).size();
		for (int i = 0; i < size; ++i) {

			AsnValue eachServingNodeAddress = ((AsnContainerValueBase) servingNodeAddressesObj)
					.getItems().get(i);
			addresses.add(eachServingNodeAddress);
		}

		return addresses;
	}

	public static InetAddress ipAddressObjectToInetAddress(AsnValue addressObj)
			throws AsnException, IOException {

		InetAddress address;

		int selectedItemNum = BerCodingUtils.judgeActualUntaggedChoiceItemTagNum(addressObj);
		switch (selectedItemNum) {
			case 0:
			case 1:
				addressObj = BerCodingUtils.interpretAsAsn1Atom(addressObj, AsnTypes.OCTET_STRING);
				address = InetAddress.getByAddress(((AsnOctetString) addressObj).getContent());
				break;

			case 2:
			case 3:
				addressObj = BerCodingUtils.interpretAsAsn1Atom(addressObj, AsnTypes.IA5STRING);
				address = InetAddress.getByName(((AsnIA5String) addressObj).toString());
				break;

			default:
				// log: unknown IPAddress selection
				throw new IllegalArgumentException("Unknown IPAddress selection (tag number: "
						+ selectedItemNum + "): " + addressObj);
		}

		return address;
	}

	public static List<AsnValue> resolveServingNodeTypes(AsnValue servingNodeTypesObj) {

		ArgumentTool.nonNullArgument(servingNodeTypesObj);

		List<AsnValue> types = new ArrayList<AsnValue>();

		int size = ((AsnContainerValueBase) servingNodeTypesObj).size();
		for (int i = 0; i < size; ++i) {

			AsnValue eachServingNodeAddress = ((AsnContainerValueBase) servingNodeTypesObj)
					.getItems().get(i);
			types.add(eachServingNodeAddress);
		}

		return types;
	}

	public static ServingNodeType servingNodeTypeObjectToServingNodeType(AsnValue servingNodeTypeObj) {

		int typeAsInt = ((AsnEnumerated) servingNodeTypeObj).getValueAsInt();

		return ServingNodeType.valueFor(typeAsInt);
	}

	public static Number getRecordSequenceNumberValueOfInputCdr(AsnValue inputCdr)
			throws Exception {

		ArgumentTool.nonNullArgument(inputCdr);

		AsnInteger recordSequenceNumber;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// recordSequenceNumber [17] INTEGER OPTIONAL,
				recordSequenceNumber = (AsnInteger) BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) inputCdr, 17, AsnTypes.INTEGER);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (recordSequenceNumber == null) {
			return null;
		} else {
			return recordSequenceNumber.getValue();
		}
	}

	public static Number getLocalSequenceNumberValueOfInputCdr(AsnValue inputCdr)
			throws Exception {

		ArgumentTool.nonNullArgument(inputCdr);

		AsnInteger localSequenceNumber;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// localSequenceNumber [20] LocalSequenceNumber OPTIONAL,
				localSequenceNumber = (AsnInteger) BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) inputCdr, 20, AsnTypes.INTEGER);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (localSequenceNumber == null) {
			return null;
		} else {
			return localSequenceNumber.getValue();
		}
	}

	public static TimeStamp getRecordOpeningTimeValueOfInputCdr(AsnValue inputCdr)
			throws Exception {

		ArgumentTool.nonNullArgument(inputCdr);

		AsnOctetString recordOpeningTimeOctetString;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// recordOpeningTime [13] TimeStamp,
				recordOpeningTimeOctetString = BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) inputCdr, 13, AsnTypes.OCTET_STRING);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (recordOpeningTimeOctetString == null) {
			return null;
		} else {
			return new TimeStamp(recordOpeningTimeOctetString.getContent());
		}
	}

	public static Number getDurationValueOfInputCdr(AsnValue inputCdr) throws Exception {

		ArgumentTool.nonNullArgument(inputCdr);

		AsnInteger duration;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// duration [14] CallDuration,
				duration = BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) inputCdr, 14, AsnTypes.INTEGER);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (duration == null) {
			return null;
		} else {
			return duration.getValue();
		}
	}

	public static Number getCauseForRecClosingValueOfInputCdr(AsnValue inputCdr) throws Exception {

		AsnInteger causeForRecClosing;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// causeForRecClosing [15] CauseForRecClosing,
				causeForRecClosing = BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) inputCdr, 15, AsnTypes.INTEGER);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		return causeForRecClosing.getValue();
	}

	public static Boolean getSgwChange(AsnValue inputCdr) throws Exception{
		AsnBoolean sgwChange = null;
		int cdrType = judgeInputCdrType(inputCdr);
		if(cdrType == CdrType.SGW_CDR){
			sgwChange = BerCodingUtils.getAtomElementByTagNum(
					(AsnContainerValueBase) inputCdr, 12, AsnTypes.BOOLEAN);
		}
		if(sgwChange != null)
			return sgwChange.getValue();
		else
			return false;
	}

	public static Number getRatTypeValueOfInputCdr(AsnValue inputCdr) throws Exception {

		AsnInteger ratType;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {
			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// rATType [30] RATType OPTIONAL,
				ratType = BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) inputCdr, 30, AsnTypes.INTEGER);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (ratType == null) {
			return null;
		} else {
			return ratType.getValue();
		}
	}

	public static List<ServingNodeType> getServingNodeTypesValueOfInputCdr(AsnValue inputCdr)
			throws AsnException {

		AsnContainerValueBase servingNodeTypesObj;

		int cdrType = judgeInputCdrType(inputCdr);
		switch (cdrType) {

			case CdrType.PGW_CDR:
			case CdrType.SGW_CDR:
				// servingNodeType [35] SEQUENCE OF ServingNodeType,
				servingNodeTypesObj = BerCodingUtils.getComplexElementByTagNum(
						(AsnContainerValueBase) inputCdr, 35);
				break;

			default:
				throw new IllegalArgumentException("unknown CDR type: " + cdrType);
		}

		if (servingNodeTypesObj == null) {

			return null;
		} else {

			List<ServingNodeType> servingNodeTypes = new ArrayList<ServingNodeType>();
			for (int i = 0; i < servingNodeTypesObj.size(); ++i) {
				servingNodeTypes.add(servingNodeTypeObjectToServingNodeType(servingNodeTypesObj
						.getItems().get(i)));
			}

			return servingNodeTypes;
		}
	}

	public static List<Long> findMissingSequenceNumbers(List<Long> numberList) {

		if (numberList == null || numberList.size() <= 1) {

			return Collections.emptyList();
		}

		List<Long> missingSNs = new ArrayList<Long>();
		// @formatter:off
		/*Object anElement = numberList.get(0);
		if (Integer.class.isAssignableFrom(anElement.getClass())
				|| Long.class.isAssignableFrom(anElement.getClass())
				|| Short.class.isAssignableFrom(anElement.getClass())
				|| BigInteger.class.isAssignableFrom(anElement.getClass())
				|| AtomicInteger.class.isAssignableFrom(anElement.getClass())
				|| AtomicLong.class.isAssignableFrom(anElement.getClass())) {
		}*/
		// @formatter:off
		Collections.sort(numberList);

		long prv = numberList.get(0);
		for (int i = 1; i < numberList.size(); ++i) {
			long curr = numberList.get(i);

			if (curr - prv != 1) {
				for (long missing = prv + 1; missing < curr; ++missing) {
					missingSNs.add(missing);
				}
			}
			prv = curr;
		}

		return missingSNs;
	}

	/**
	 * <p>
	 * 解析input P-GW CDR的内容, 生成简单的报表; 调试用.
	 * </p>
	 *
	 * @param inputPgwCdr
	 * @return
	 * @throws Exception
	 */
	public static String analysisInputPgwCdr(AsnValue inputPgwCdr) throws Exception {

		StringWriter buf = new StringWriter();
		PrintWriter out = new PrintWriter(buf);

		out.println("Input CDR: {");
		out.println("******outer-most view******");
		out.println("tag: " + inputPgwCdr.getTag());
		out.println("class: " + inputPgwCdr.getClass());
		out.println("******outer-most view end******");
		out.println();

		AsnContainerValueBase coll = null;
		if (inputPgwCdr instanceof AsnContainerValueBase) {
			coll = (AsnContainerValueBase) inputPgwCdr;
		} else {
			return "bad input P-GW CDR (NOT an ASN.1 constructed type): " + inputPgwCdr;
		}

		out.println("******list all elements******");
		for (AsnValue element : coll.getItems()) {
			out.println("tag: " + element.getTag());
		}
		out.println("******list all elements end******");
		out.println();

		out.println("******print elements details******");
		// [0] recordType:
		AsnValue recordType = BerCodingUtils.getAtomElementByTagNum(coll, 0, AsnTypes.INTEGER);
		if (recordType != null) {
			out.println("tag: [0] recordType: " + recordType.getTag());
			out.println("class: " + recordType.getClass());
			out.println("value: " + recordType);
			out.println();
		} else {
			out.println("tag: [0] recordType: does NOT exist!");
			out.println();
		}

		// [3] servedIMSI:
		AsnValue servedIMSI = BerCodingUtils.getAtomElementByTagNum(coll, 3, AsnTypes.OCTET_STRING);
		if (servedIMSI != null) {
			out.println("tag: [3] servedIMSI: " + servedIMSI.getTag());
			out.println("class: " + servedIMSI.getClass());
			out.println("value: "
					+ BerCodingUtils.getReadableByteArray(
							((AsnOctetString) servedIMSI).getContent(), 16, " "));
			out.println();
		} else {
			out.println("tag: [3] servedIMSI: does NOT exist!");
			out.println();
		}

		// [4] p-GWAddress:
		AsnValue p_GWAddress = getPgwAddressOfInputCdr(coll);
//		p_GWAddress = BerCodingUtils.getTaggedChoiceElement(coll, 4);
		if (p_GWAddress != null) {
			out.println("tag: [4] p-GWAddress:" + p_GWAddress.getTag());
			out.println("class: " + p_GWAddress.getClass());
			out.println("value: " + ipAddressObjectToString(p_GWAddress));
			out.println();
		} else {
			out.println("tag: [4] p-GWAddress: does NOT exist!");
			out.println();
		}

		// [5] chargingID:
		AsnValue chargingID = BerCodingUtils.getAtomElementByTagNum(coll, 5, AsnTypes.INTEGER);
		if (chargingID != null) {
			out.println("tag: [5] chargingID");
			out.println("class: " + chargingID.getClass());
			out.println("value: " + chargingID);
			out.println();
		} else {
			out.println("tag: [5] chargingID: does NOT exist!");
			out.println();
		}

		// [6] servingNodeAddress:
		AsnValue servingNodeAddress = BerCodingUtils.getComplexElementByTagNum(coll, 6);
		if (servingNodeAddress != null) {
			out.println("tag: [6] servingNodeAddress");
			out.println("class: " + servingNodeAddress.getClass());
			out.println("value: " + servingNodeAddressesToString(servingNodeAddress));
			out.println();
		} else {
			out.println("tag: [6] servingNodeAddress: does NOT exist!");
			out.println();
		}

		// [7] accessPointNameNI:
		AsnValue accessPointNameNI = BerCodingUtils.getAtomElementByTagNum(coll, 7,
				AsnTypes.IA5STRING);
		if (accessPointNameNI != null) {
			out.println("tag: [7] accessPointNameNI");
			out.println("class: " + accessPointNameNI.getClass());
			out.println("value: " + accessPointNameNI);
			out.println();
		} else {
			out.println("tag: [7] accessPointNameNI: does NOT exist!");
			out.println();
		}

		// [8] pdpPDNType:
		AsnValue pdpPDNType = BerCodingUtils.getAtomElementByTagNum(coll, 8,
				AsnTypes.OCTET_STRING);
		if (pdpPDNType != null) {
			out.println("tag: [8] pdpPDNType");
			out.println("class: " + pdpPDNType.getClass());
			out.println("value: "
					+ BerCodingUtils.getReadableByteArray(
							((AsnOctetString) pdpPDNType).getContent(), 16, " "));
			out.println();
		} else {
			out.println("tag: [8] pdpPDNType: does NOT exist!");
			out.println();
		}

		// [9] servedPDPPDNAddress:
		AsnValue servedPDPPDNAddress = BerCodingUtils.getTaggedChoiceElement(coll, 9);
		if (servedPDPPDNAddress != null) {
			out.println("tag: [9] servedPDPPDNAddress");
			out.println("class: " + servedPDPPDNAddress.getClass());
			out.println("value: " + ipAddressObjectToString(servedPDPPDNAddress));
			out.println();
		} else {
			out.println("tag: [9] servedPDPPDNAddress: does NOT exist!");
			out.println();
		}

		// [11] dynamicAddressFlag:
		AsnValue dynamicAddressFlag = BerCodingUtils.getAtomElementByTagNum(coll, 9,
				AsnTypes.BOOLEAN);
		if (dynamicAddressFlag != null) {
			out.println("tag: [11] dynamicAddressFlag");
			out.println("class: " + dynamicAddressFlag.getClass());
			out.println("value: " + dynamicAddressFlag);
			out.println();
		} else {
			out.println("tag: [11] dynamicAddressFlag: does NOT exist!");
			out.println();
		}

		// [13] recordOpeningTime:
		AsnValue recordOpeningTime = BerCodingUtils.getAtomElementByTagNum(coll, 13,
				AsnTypes.OCTET_STRING);
		if (recordOpeningTime != null) {
			out.println("tag: [13] recordOpeningTime");
			out.println("class: " + recordOpeningTime.getClass());
			out.println("value: "
					+ BerCodingUtils.getReadableByteArray(
							((AsnOctetString) recordOpeningTime).getContent(), 10, " "));
			out.println();
		} else {
			out.println("tag: [13] recordOpeningTime: does NOT exist!");
			out.println();
		}

		// [14] duration:
		AsnValue duration = BerCodingUtils.getAtomElementByTagNum(coll, 14,
				AsnTypes.INTEGER);
		if (duration != null) {
			out.println("tag: [14] duration");
			out.println("class: " + duration.getClass());
			out.println("value: " + duration);
			out.println();
		} else {
			out.println("tag: [14] duration: does NOT exist!");
			out.println();
		}

		// [15] causeForRecClosing:
		AsnValue causeForRecClosing = BerCodingUtils.getAtomElementByTagNum(coll, 15,
				AsnTypes.INTEGER);
		if (causeForRecClosing != null) {
			out.println("tag: [15] causeForRecClosing");
			out.println("class: " + causeForRecClosing.getClass());
			out.println("value: " + causeForRecClosing);
			out.println();
		} else {
			out.println("tag: [15] causeForRecClosing: does NOT exist!");
			out.println();
		}

		// [16] diagnostics:
		AsnValue diagnostics = BerCodingUtils.getTaggedChoiceElement(coll, 16);
		if (diagnostics != null) {
			out.println("tag: [16] diagnostics");
			out.println("class: " + diagnostics.getClass());
			out.println("value: " + diagnostics);
			out.println();
		} else {
			out.println("tag: [16] diagnostics: does NOT exist!");
			out.println();
		}

		// [17] recordSequenceNumber:
		AsnValue recordSequenceNumber = BerCodingUtils.getAtomElementByTagNum(coll, 17,
				AsnTypes.INTEGER);
		if (recordSequenceNumber != null) {
			out.println("tag: [17] recordSequenceNumber");
			out.println("class: " + recordSequenceNumber.getClass());
			out.println("value: " + recordSequenceNumber);
			out.println();
		} else {
			out.println("tag: [17] recordSequenceNumber: does NOT exist!");
			out.println();
		}

		// [18] nodeID:
		AsnValue nodeID = BerCodingUtils.getAtomElementByTagNum(coll, 18, AsnTypes.IA5STRING);
		if (nodeID != null) {
			out.println("tag: [18] nodeID");
			out.println("class: " + nodeID.getClass());
			out.println("value: " + nodeID);
			out.println();
		} else {
			out.println("tag: [18] nodeID: does NOT exist!");
			out.println();
		}

		// [19] recordExtensions:

		// [20] localSequenceNumber:
		AsnValue localSequenceNumber = BerCodingUtils.getAtomElementByTagNum(coll, 20,
				AsnTypes.INTEGER);
		if (localSequenceNumber != null) {
			out.println("tag: [20] localSequenceNumber");
			out.println("class: " + localSequenceNumber.getClass());
			out.println("value: " + localSequenceNumber);
			out.println();
		} else {
			out.println("tag: [20] localSequenceNumber: does NOT exist!");
			out.println();
		}

		// [21] apnSelectionMode:

		// [22] servedMSISDN:

		// [23] chargingCharacteristics:
		AsnValue chargingCharacteristics = BerCodingUtils.getAtomElementByTagNum(coll, 23,
				AsnTypes.OCTET_STRING);
		if (chargingCharacteristics != null) {
			out.println("tag: [23] chargingCharacteristics");
			out.println("class: " + chargingCharacteristics.getClass());
			out.println("value: "
					+ BerCodingUtils.getReadableByteArray(
							((AsnOctetString) chargingCharacteristics).getContent(), 16, " "));
			out.println();
		} else {
			out.println("tag: [23] chargingCharacteristics: does NOT exist!");
			out.println();
		}

		// [24] chChSelectionMode:

		// [25] iMSsignalingContext:

		// [26] externalChargingID:

		// [27] servinggNodePLMNIdentifier:

		// [28] pSFurnishChargingInformation:

		// [29] servedIMEISV:

		// [30] rATType:
		AsnValue rATType = BerCodingUtils.getAtomElementByTagNum(coll, 30,
				AsnTypes.INTEGER);
		if (rATType != null) {
			out.println("tag: [30] rATType");
			out.println("class: " + rATType.getClass());
			out.println("value: " + rATType);
			out.println();
		} else {
			out.println("tag: [30] rATType: does NOT exist!");
			out.println();
		}

		// [31] mSTimeZone:

		// [32] userLocationInformation:

		// [33] cAMELChargingInformation:

		// [34] listOfServiceData:

		// [35] servingNodeType:
		AsnValue servingNodeType = BerCodingUtils.getComplexElementByTagNum(coll, 35);
		if (servingNodeType != null) {
			out.println("tag: [35] servingNodeType");
			out.println("class: " + servingNodeType.getClass());
			out.println("value: " + servingNodeTypesToString(servingNodeType));
			out.println();
		} else {
			out.println("tag: [35] servingNodeType: does NOT exist!");
			out.println();
		}

		// [36] servedMNNAI:

		// [37] p-GWPLMNIdentifier:

		// [38] startTime:
		AsnValue startTime = BerCodingUtils.getAtomElementByTagNum(coll, 38,
				AsnTypes.OCTET_STRING);
		if (startTime != null) {
			out.println("tag: [38] startTime");
			out.println("class: " + startTime.getClass());
			out.println("value: " + BerCodingUtils.getReadableByteArray(
					((AsnOctetString) startTime).getContent(), 10, " "));
			out.println();
		} else {
			out.println("tag: [38] startTime: does NOT exist!");
			out.println();
		}

		// [39] stopTime:
		AsnValue stopTime = BerCodingUtils.getAtomElementByTagNum(coll, 39,
				AsnTypes.OCTET_STRING);
		if (stopTime != null) {
			out.println("tag: [39] stopTime");
			out.println("class: " + stopTime.getClass());
			out.println("value: " + BerCodingUtils.getReadableByteArray(
					((AsnOctetString) stopTime).getContent(), 10, " "));
			out.println();
		} else {
			out.println("tag: [39] stopTime: does NOT exist!");
			out.println();
		}

		// [40] served3gpp2MEID:

		// [41] pDNConnectionID:
		out.println("******print elements details end******");

		out.println("}");

		return buf.toString();
	}

	private MergingUtils() {

	}
}
