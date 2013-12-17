package com.baoyun.subsystems.cgf.handler.processor;

import static com.baoyun.subsystems.cgf.utils.CdrSerializationUtils.getAllCdrInOneRequest;
import static com.baoyun.subsystems.cgf.utils.CdrSerializationUtils.judgeCDRTransRequestType;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnValue;
import ch.panter.li.bi.asn.value.AsnContainerValueBase;

import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferCDRRequestBase;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferRequest;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeMessage;
import com.baoyun.subsystems.cgf.handler.GtpDataTransferRequestProcessor;
import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrMergingManager;
import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrType;
import com.baoyun.subsystems.cgf.handler.processor.cdr.DefaultCdrMergingManager;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CompletePgwCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.EndOfMergingCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.SgwChangeCdrReceived;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.SgwEndOfMergingCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.TerminatePgwCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.TerminateSgwCdrReceived;
import com.baoyun.subsystems.cgf.utils.BerCodingUtils;
import com.baoyun.subsystems.cgf.utils.HandleFileUtils;
import com.baoyun.subsystems.cgf.utils.MergingUtils;

/**
 * <p>
 * 持有1个CdrMergingManager, 将接受到的CDR进行解码, 并一一传递给CdrMergingManager进行处理.
 * </p>
 *
 *
 *
 */
public abstract class CdrTransferRequestProcessor implements
		GtpDataTransferRequestProcessor {

	private static Logger log = LoggerFactory.getLogger(CdrTransferRequestProcessor.class);

	protected CdrMergingManager mergingMgr;

	public CdrTransferRequestProcessor() {

		mergingMgr = new DefaultCdrMergingManager();
	}

	@Override
	public GtpPrimeMessage process(
			GtpPrimeDataRecordTransferRequest dataRecordTranReq)
			throws Exception {

		GtpPrimeMessage resp = null;
		// TODO details of GtpPrimeDataRecordTransferCDRRequestBase subclasses
		if (dataRecordTranReq instanceof GtpPrimeDataRecordTransferCDRRequestBase) {
			resp = processCdr(dataRecordTranReq);
		}

		return resp;
	}

	/**
	 * <pre>
	 * 	主流程:
	 * 		0. 判断CDR类型(S-GW, P-GW);
	 * 		1. 反序列化;
	 * 		2. 与CdrMergingManager交互;
	 * 		3. GTP' 协议的data transfer request也需要有响应: cdr.getResponse((short) 0);
	 * </pre>
	 *
	 * @param cdr
	 * @return
	 * @throws Exception
	 */
	protected GtpPrimeMessage processCdr(GtpPrimeDataRecordTransferRequest cdr)
			throws Exception {

		if (judgeCDRTransRequestType(cdr) == CdrType.PGW_CDR) {
			// do PgwCdr合并

			// 可以获取到一个GtpMessage中所有cdr的ArrayList<byte[]>列表，可以直接进行decode
			List<byte[]> receivedCdrs = getAllCdrInOneRequest(cdr);

//			testFileSysPersistence(receivedCdrs);

			List<AsnValue> receivedCdrObjs = new ArrayList<AsnValue>();
			for (byte[] each : receivedCdrs) {
				receivedCdrObjs.add(BerCodingUtils.decode(each));
			}

			log.trace("a batch of CDRs received, amount = {}", receivedCdrObjs.size());

			for (AsnValue each : receivedCdrObjs) {
				CdrProcessingEvent cdrReceived = genCdrProcessingEvent(each);

				mergingMgr.respond(cdrReceived);
			}

		} else if (judgeCDRTransRequestType(cdr) == CdrType.SGW_CDR) {
			// do SgwCdr合并
			List<byte[]> receivedCdrs = getAllCdrInOneRequest(cdr);
			List<AsnValue> receivedCdrObjs = new ArrayList<AsnValue>();
			for (byte[] each : receivedCdrs) {
				receivedCdrObjs.add(BerCodingUtils.decode(each));
			}
			log.trace("a batch of CDRs received, amount = {}", receivedCdrObjs.size());

			for (AsnValue each : receivedCdrObjs) {
				CdrProcessingEvent cdrReceived = genCdrProcessingEvent(each);

				mergingMgr.respond(cdrReceived);
			}
		} else {
			// 异常处理
			log.warn("CdrType is neither 84 nor 85!");
		}

		return cdr;
	}

	/**
	 * <pre> CauseForRecClosing ::= INTEGER {
	 * 	--
	 * 	-- In PGW-CDR and SGW-CDR the value servingNodeChange is used for partial record
	 * 	-- generation due to Serving Node Address list Overflow
	 * 	-- In SGSN servingNodeChange indicates the SGSN change
	 * 	--
	 * 	-- LCS related causes belong to the MAP error causes acc. TS 29.002 [60]
	 * 	--
	 * 	-- cause codes 0 to 15 are defined 'CauseForTerm' (cause for termination)
	 * 	--
	 * 	normalRelease                   (0),
	 * 	abnormalRelease                 (4),
	 * 	cAMELInitCallRelease            (5),
	 * 	volumeLimit                     (16),
	 * 	timeLimit                       (17),
	 * 	servingNodeChange               (18),
	 * 	maxChangeCond                   (19),
	 * 	managementIntervention          (20),
	 * 	intraSGSNIntersystemChange      (21),
	 * 	rATChange                       (22),
	 * 	mSTimeZoneChange                (23),
	 * 	sGSNPLMNIDChange                (24),
	 * 	unauthorizedRequestingNetwork   (52),
	 * 	unauthorizedLCSClient           (53),
	 * 	positionMethodFailure           (54),
	 * 	unknownOrUnreachableLCSClient   (58),
	 * 	listofDownstreamNodeChange      (59)
	 * }</pre>
	 *
	 * @param inputCdr
	 * @return
	 * @throws Exception
	 */
	protected CdrProcessingEvent genCdrProcessingEvent(AsnValue inputCdr) throws Exception {

		if (!MergingUtils.isPartialCdr(inputCdr)) {

			int cdrType = MergingUtils.judgeInputCdrType(inputCdr);
			switch (cdrType) {
				case CdrType.PGW_CDR:
					return new CompletePgwCdrReceived(MergingUtils.genInputPgwCdrObject(inputCdr));

				case CdrType.SGW_CDR:
					throw new IllegalArgumentException("S-GW CDR NOT supported yet!");

				default:
					throw new IllegalArgumentException("unknown CDR type: " + cdrType);
			}
		} else {

			int cdrType = MergingUtils.judgeInputCdrType(inputCdr);

			// FIXME: 根据具体是P-GW CDR, S-GW CDR, 进行switch: 对 maxChangeCond (19) 的处理.
			int causeForRecClosing = MergingUtils.getCauseForRecClosingValueOfInputCdr(inputCdr)
					.intValue();
			Boolean SgwChange = MergingUtils.getSgwChange(inputCdr).booleanValue();
			if(SgwChange)
				return new SgwChangeCdrReceived(MergingUtils.genInputSgwCdrObject(inputCdr));
			switch (causeForRecClosing) {
				case 16:
				case 17:
					if(cdrType == CdrType.PGW_CDR)
						return new CdrReceived(MergingUtils.genInputPgwCdrObject(inputCdr));
					if(cdrType == CdrType.SGW_CDR)
						return new CdrReceived(MergingUtils.genInputSgwCdrObject(inputCdr));

				case 0:
				case 4:
					if(cdrType == CdrType.PGW_CDR)
						return new TerminatePgwCdrReceived(MergingUtils.genInputPgwCdrObject(inputCdr));
					if(cdrType == CdrType.SGW_CDR)
						return new TerminateSgwCdrReceived(MergingUtils.genInputSgwCdrObject(inputCdr));
				case 18:
				case 19:
				case 22:
					if(cdrType == CdrType.PGW_CDR)
						return new EndOfMergingCdrReceived(MergingUtils.genInputPgwCdrObject(inputCdr));
					if(cdrType == CdrType.SGW_CDR)
						return new SgwEndOfMergingCdrReceived(MergingUtils.genInputSgwCdrObject(inputCdr));
				default:
					throw new IllegalArgumentException("unsupported causeForRecClosing value: "
							+ causeForRecClosing);
			}
		}
	}

	@SuppressWarnings("unused")
	private void testFileSysPersistence(List<byte[]> allReceivedCdrs) throws Exception {

		List<AsnValue> receivedCdrObjs = new ArrayList<AsnValue>();

		for (byte[] each : allReceivedCdrs) {
			receivedCdrObjs.add(BerCodingUtils.decode(each));
		}

		List<AsnValue> generatedTargetCdrs = new ArrayList<AsnValue>();
		for (AsnValue each : receivedCdrObjs) {
			generatedTargetCdrs.add(MergingUtils.createOutputPgwCdrFromInputPgwCdr(
					(AsnContainerValueBase) each, 0));
		}

		List<byte[]> tmpEncoded = new ArrayList<byte[]>();
		for (AsnValue each : generatedTargetCdrs) {
			tmpEncoded.add(BerCodingUtils.encode(each));
		}

		for (byte[] each : tmpEncoded) {
			HandleFileUtils.saveFinalFile(each);
		}
	}
}
