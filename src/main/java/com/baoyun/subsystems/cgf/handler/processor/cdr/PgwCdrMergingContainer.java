package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnException;
import ch.panter.li.bi.asn.AsnTagClass;
import ch.panter.li.bi.asn.AsnTagNature;
import ch.panter.li.bi.asn.AsnValue;
import ch.panter.li.bi.asn.model.AsnTypes;
import ch.panter.li.bi.asn.value.AsnContainerValueBase;
import ch.panter.li.bi.asn.value.AsnEnumerated;
import ch.panter.li.bi.asn.value.AsnInteger;

import com.baoyun.subsystems.cgf.asn1.ConsolidationResult;
import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.asn1.TimeStamp;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CompletePgwCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.EndOfMergingCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.ParticularCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.TerminatePgwCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergingContainerState;
import com.baoyun.subsystems.cgf.utils.BerCodingUtils;
import com.baoyun.subsystems.cgf.utils.MergingUtils;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class PgwCdrMergingContainer {

	private Logger log = LoggerFactory.getLogger(PgwCdrMergingContainer.class);

	private static String toStringPattern = "%s: {originalInputPgwCdrs=%s, consecutive=%s, receivedRSNs=%s, receivedOriCdrNum=%s, missingRSNs=%s, currentState=%s, minRecordSequenceNumber=%s, maxRecordSequenceNumber=%s, needMerge=%s, alreadyCalledDoMerge=%s}";

	private AsnValue mergingResultStub;

	private List<InputPgwCdrObject> originalInputPgwCdrs = new ArrayList<InputPgwCdrObject>();

	private boolean consecutive = true;
 
	private Set<Long> receivedRSNs = new HashSet<Long>();

	private int receivedOriCdrNum = 0;

	private Set<Long> missingRSNs = new HashSet<Long>();

	private PgwCdrMergingContainerState currentState = PgwCdrMergingContainerState.INITIAL;

	private PgwCdrMergingKey key;

	private PgwCdrMergerContext context;

	private long minRecordSequenceNumber = Long.MAX_VALUE;

	private long maxRecordSequenceNumber = Long.MIN_VALUE;

	private boolean needMerge;

	private boolean alreadyCalledDoMerge = false;

	public PgwCdrMergingContainer(InputPgwCdrObject inputPgwCdr, PgwCdrMergerContext context) {

		try {
			mergingResultStub = MergingUtils.createOutputPgwCdrFromInputPgwCdr(
					(AsnContainerValueBase) inputPgwCdr.getInputPgwCdr(),
					ConsolidationResult.INIT.getValue());
		} catch (Exception e) {
			log.error(
					"errors occur while creating output P-GW CDR from input P-GW CDR: {}, exception: {}.",
					inputPgwCdr.getInputPgwCdr(), MiscUtils.exceptionStackTrace2String(e));
		}

		key = new PgwCdrMergingKey(inputPgwCdr);
		this.context = context;
		needMerge = !inputPgwCdr.isPartial();
	}

	@Override
	public String toString() {

		return String.format(toStringPattern, key, originalInputPgwCdrs, consecutive, receivedRSNs,
				receivedOriCdrNum, missingRSNs, currentState, minRecordSequenceNumber,
				maxRecordSequenceNumber, needMerge, alreadyCalledDoMerge);
	}

	// bussiness logic method: dealing with CDR merge:

	/**
	 * 当PgwCdrMergingContainer状态可能会改变时, 外界应统一通过调用此方法, 触发PgwCdrMergingContainer的自行处理
	 * (当MergerContext必须直接修改其下属的MergingContainer的状态时除外).
	 */
	public void action(CdrProcessingEvent event) {

		InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();

		if (event instanceof CdrReceived) {

			addOriginalPgwCdr(inputPgwCdr);

			switch (currentState) {

				case INITIAL:

					if (event instanceof CompletePgwCdrReceived) {

						currentState = PgwCdrMergingContainerState.ONE_COMPLETE_P_GW_CDR_RECEIVED_NO_NEED_MERGING;

						try {
							doOneCompleteCdrMerge(event);
						} catch (Exception e) {
							// TODO: 对 doMerge() 执行的异常处理.
							currentState = PgwCdrMergingContainerState.UNIGNORABLE_ERROR_OCCURED;
							log.error(
									"errors occur in doOneCompleteCdrMerge(), PgwCdrMergingContainer: {}, exception: {}.",
									this, MiscUtils.exceptionStackTrace2String(e));
						}

						currentState = PgwCdrMergingContainerState.MERGE_DONE;

					} else if (event instanceof ParticularCdrReceived) {

						if (event instanceof EndOfMergingCdrReceived) {

							currentState = PgwCdrMergingContainerState.MERGE_TRIGGERING_PARTIAL_CDR_RECEIVED;
						} else if (event instanceof TerminatePgwCdrReceived) {

							currentState = PgwCdrMergingContainerState.TERMINATE_PARTIAL_CDR_RECEIVED;
						}

						try {
							doMerge(event);
							currentState = PgwCdrMergingContainerState.MERGE_DONE;
						} catch (Exception e) {
							// TODO: 对 doMerge() 执行的异常处理.
							currentState = PgwCdrMergingContainerState.UNIGNORABLE_ERROR_OCCURED;
							log.error(
									"errors occur in doMerge(), PgwCdrMergingContainer: {}, exception: {}.",
									this, MiscUtils.exceptionStackTrace2String(e));
						}

					} else {

						currentState = PgwCdrMergingContainerState.RECEIVING;
					}

					break;

				// TODO: 是否有可能接收到的第一个部分话单, 就发生了S-GW切换, RAT切换, ...?

				case RECEIVING:

					if (event instanceof ParticularCdrReceived) {

						// 设置 MergingContainer 的状态.
						if (event instanceof EndOfMergingCdrReceived) {

							currentState = PgwCdrMergingContainerState.MERGE_TRIGGERING_PARTIAL_CDR_RECEIVED;
						} else if (event instanceof TerminatePgwCdrReceived) {

							currentState = PgwCdrMergingContainerState.TERMINATE_PARTIAL_CDR_RECEIVED;
						}

						// 对接收到的特殊部分 CDR 进行处理.
						if (event instanceof EndOfMergingCdrReceived
								|| event instanceof TerminatePgwCdrReceived) {

							try {
								currentState = PgwCdrMergingContainerState.MERGING;
								doMerge(event);
								currentState = PgwCdrMergingContainerState.MERGE_DONE;
							} catch (Exception e) {
								// TODO: 对 doMerge() 执行的异常处理.
								currentState = PgwCdrMergingContainerState.UNIGNORABLE_ERROR_OCCURED;
								log.error(
										"errors occur in doMerge(), PgwCdrMergingContainer: {}, exception: {}.",
										this, MiscUtils.exceptionStackTrace2String(e));
							}
						}
					}

					break;

				default:
					log.warn(
							"state error? inappropriate invocation? currentState: {}, PgwCdrMergingContainer: {}.",
							currentState, this);
			}
		}
	}

	// TODO: 1. doMerge(), doOneCompleteCdrMerge()方法的modifier: public?? 2. do*()的异常处理.

	/**
	 * <p>
	 * 进行一次: 原始 CDR 的扫描, mergingResultStub 的写入 等工作.
	 * </p>
	 *
	 * <p>
	 * 会置 alreadyCalledDoMerge 标识, 若需重新执行一遍合并逻辑, 需先调用 resetMergingResult().
	 * </p>
	 *
	 * @param event
	 * @throws Exception
	 */
	protected void doMerge(CdrProcessingEvent event) throws Exception {

		if (alreadyCalledDoMerge) {
			return;
		}

		alreadyCalledDoMerge = true;

		// 若只包含完整话单: 认为合并动作已完成
		if (isNeedMerge()) {
			// 对部分话单进行合并:
			AsnValue mergingResultStub = getMergingResultStub();

			/*
			 * 1. 按recordSequenceNumber, 对PendingList中所有的P-GW CDR进行排序:
			 */
			List<InputPgwCdrObject> originalInputPgwCdrs = getOriginalInputPgwCdrs();
			Collections.sort(originalInputPgwCdrs);

			AsnValue firstPartialPgwCdr = originalInputPgwCdrs.get(0).getInputPgwCdr();
			AsnValue lastPartialPgwCdr = originalInputPgwCdrs.get(originalInputPgwCdrs.size() - 1)
					.getInputPgwCdr();
			// TODO: refactor to use InputPgwCdrObject:
			setMinRecordSequenceNumber(MergingUtils
					.getRecordSequenceNumberValueOfInputCdr(firstPartialPgwCdr).intValue());
			setMaxRecordSequenceNumber(MergingUtils
					.getRecordSequenceNumberValueOfInputCdr(lastPartialPgwCdr).intValue());

			/*
			 * 2. 处理合并结果的recordOpeningTime 和 duration字段:
			 */

			// 话单是否连续: 根据recordOpeningTime + duration与下一个部分话单的recordOpeningTime是否相差2秒以内判断:
			boolean isConsecutive = true;
			// 部分话单的duration累计值: TODO: 使用int类型是否够大?
			int totalDuration = MergingUtils.getDurationValueOfInputCdr(firstPartialPgwCdr)
					.intValue();

			AsnValue previousPartialPgwCdr = firstPartialPgwCdr;
			for (int i = 1; i < originalInputPgwCdrs.size(); ++i) {

				receivedRSNs.add(originalInputPgwCdrs.get(i).getRecordSequenceNumber());

				AsnValue currentPartialPgwCdr = originalInputPgwCdrs.get(i).getInputPgwCdr();
				totalDuration += MergingUtils
						.getDurationValueOfInputCdr(currentPartialPgwCdr).intValue();

				if (!isConsecutive
						|| MergingUtils.getRecordSequenceNumberValueOfInputCdr(
								currentPartialPgwCdr)
								.intValue()
								- MergingUtils.getRecordSequenceNumberValueOfInputCdr(
										previousPartialPgwCdr).intValue() != 1) {
					// recordSequenceNumber不连续, 则认为部分话单非连续:
					isConsecutive = false;
				}
			}

			consecutive = isConsecutive;

			Set<Long> tmpSet = new HashSet<Long>();
			tmpSet.addAll(MergingUtils
					.findMissingSequenceNumbers(new ArrayList<Long>(receivedRSNs)));
			missingRSNs = tmpSet;

			if (isConsecutive) {
				// @formatter:off
				/*
				 * 8.2.5. PGW-CDR的合并
				 *
				 * 4）需要过滤的字段 若部分话单连续, 则: 对于一批连续部分话单，Duration字段 = （最后的部分记录的Record Opening Time -
				 * 最先的部分记录的Record Opening Time + 最后的部分记录的Duration字段）； 对于不连续话单Duration字段累加。
				 */
				// @formatter:on
				TimeStamp firstRecordOpeningTime = MergingUtils
						.getRecordOpeningTimeValueOfInputCdr(firstPartialPgwCdr);
				TimeStamp lastRecordOpeningTime = MergingUtils
						.getRecordOpeningTimeValueOfInputCdr(lastPartialPgwCdr);
				int lastDuration = MergingUtils
						.getDurationValueOfInputCdr(lastPartialPgwCdr).intValue();

				totalDuration = lastDuration
						+ (int) (lastRecordOpeningTime.getTimeAsSecond() - firstRecordOpeningTime
								.getTimeAsSecond());
			}

			// 合并后的duration字段:
			AsnInteger mergedDuration = BerCodingUtils.createAsn1Value(AsnTypes.INTEGER);
			mergedDuration.setValue(totalDuration);
			mergedDuration.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
					AsnTagNature.Primitive, 14));

			BerCodingUtils.replaceElementByTagNum((AsnContainerValueBase) mergingResultStub,
					mergedDuration, 14);

			// TODO: 合并后的recordOpeningTime字段, 取recordSequenceNumber最小的部分话单的;
			// 能否确保recordOpeningTime也是最小的?
			AsnValue minRecordOpeningTime = BerCodingUtils.getElementByTagNum(
					(AsnContainerValueBase) firstPartialPgwCdr, 13);

			if (!BerCodingUtils.getElementByTagNum((AsnContainerValueBase) mergingResultStub,
					13).equals(minRecordOpeningTime)) {
				// 当接收到的第一个部分P-GW CDR不是recordSequenceNumber最小的那个, 即CG并非按部分话单的顺序对其进行接收时:
				BerCodingUtils.replaceElementByTagNum(
						(AsnContainerValueBase) mergingResultStub, minRecordOpeningTime, 13);
			}

			// @formatter:off
			/*
			 * 3. 处理合并结果的recordSequenceNumber, localSequenceNumber字段:
			 *
			 * recordSequenceNumber [17] RECORDSequenceNumber OPTIONAL,
			 * localSequenceNumber [20] LOCALRECORDSequenceNumber OPTIONAL
			 */
			// @formatter:on
			AsnContainerValueBase mergingResultRecordSequenceNumber = BerCodingUtils
					.getComplexElementByTagNum((AsnContainerValueBase) mergingResultStub, 17);
			AsnContainerValueBase mergingResultRecordNumberList = BerCodingUtils
					.getComplexElementByTagNum(mergingResultRecordSequenceNumber, 1);

			AsnContainerValueBase mergingResultLocalSequenceNumber = BerCodingUtils
					.getComplexElementByTagNum((AsnContainerValueBase) mergingResultStub, 20);
			AsnContainerValueBase mergingResultLocalRecordNumberList = BerCodingUtils
					.getComplexElementByTagNum(mergingResultLocalSequenceNumber, 1);

			for (int i = 0; i < originalInputPgwCdrs.size(); ++i) {
				// 按recordSequenceNumber的顺序, 将每个部分话单的recordSequenceNumber,
				// 和localSequenceNumber, 分别append到合并结果的recordSequenceNumber列表,
				// 以及localSequenceNumber列表中去:

				AsnValue currentPartialPgwCdr = originalInputPgwCdrs.get(i).getInputPgwCdr();

				AsnValue currentRecordSequenceNumber = BerCodingUtils.getElementByTagNum(
						(AsnContainerValueBase) currentPartialPgwCdr, 17);
				if (currentRecordSequenceNumber != null) {
					// 对于部分话单, recordSequenceNumber应该一定非空.
					BerCodingUtils.appendElement(mergingResultRecordNumberList,
							currentRecordSequenceNumber);
				}

				AsnValue currentLocalSequenceNumber = BerCodingUtils.getElementByTagNum(
						(AsnContainerValueBase) currentPartialPgwCdr, 20);
				if (currentLocalSequenceNumber != null) {
					BerCodingUtils.appendElement(mergingResultLocalRecordNumberList,
							currentLocalSequenceNumber);
				}
			}

			// TODO: 完成有完整待合并P-GW CDR列表的PendingList的合并.

			/*
			 * 4. 处理合并结果的listOfServiceData字段(FIXME: 仅仅将所有部分话单的listOfServiceData进行连接):
			 *
			 * listOfServiceData [34] SEQUENCE OF ChangeOfServiceCondition OPTIONAL
			 */

			// 合并结果的listOfServiceData: SEQUENCE OF 字段:
			AsnContainerValueBase mergingResultListOfServiceData = BerCodingUtils
					.getComplexElementByTagNum((AsnContainerValueBase) mergingResultStub, 34);
			if (mergingResultListOfServiceData == null) {

				mergingResultListOfServiceData = BerCodingUtils
						.createAsn1Value(AsnTypes.SEQUENCE_OF);
				mergingResultListOfServiceData.setTag(BerCodingUtils.createAsn1Tag(
						AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 34));
			}

			for (int i = 0; i < originalInputPgwCdrs.size(); ++i) {
				// 按recordSequenceNumber的顺序, 将每个部分话单的listOfServiceData中的每条记录,
				// 都给append到合并结果的listOfServiceData中去:

				AsnValue currentPartialPgwCdr = originalInputPgwCdrs.get(i).getInputPgwCdr();

				AsnContainerValueBase currentListOfServiceData = BerCodingUtils
						.getComplexElementByTagNum(
								(AsnContainerValueBase) currentPartialPgwCdr, 34);
				if (currentListOfServiceData != null) {
					for (int j = 0; j < currentListOfServiceData.size(); ++j) {
						BerCodingUtils.appendElement(mergingResultListOfServiceData,
								BerCodingUtils
										.getElementByPosition(currentListOfServiceData, j));
					}
				}
			}

			if (BerCodingUtils.hasElementWithTagNum((AsnContainerValueBase) mergingResultStub,
					34)) {
				BerCodingUtils.replaceElementByTagNum(
						(AsnContainerValueBase) mergingResultStub,
						mergingResultListOfServiceData, 34);
			} else {
				BerCodingUtils.addElement((AsnContainerValueBase) mergingResultStub,
						mergingResultListOfServiceData);
			}

		}

		if (isNeedMerge()) {
			setCurrentState(PgwCdrMergingContainerState.MERGE_DONE);
			log.trace("PgwCdrMergingContainer: {} state change to: {}.", getKey(),
					getCurrentState());
		}

		// FIXME: 合并完成, 部分话单丢失事件的发布:
	}

	/**
	 * <p>
	 * 完成完整话单的处理, 包括设置consolidationResult字段等.
	 * </p>
	 *
	 * @param event
	 * @throws Exception
	 */
	protected void doOneCompleteCdrMerge(CdrProcessingEvent event) throws Exception {

		AsnValue mergingResultStub = getMergingResultStub();

		AsnEnumerated consolidationResult = BerCodingUtils.createAsn1Value(AsnTypes.ENUMERATED);
		consolidationResult.setValue(ConsolidationResult.NORMAL.getValue());
		consolidationResult.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Primitive, 101));

		BerCodingUtils.replaceElementByTagNum((AsnContainerValueBase) mergingResultStub,
				consolidationResult, 101);
	}

	/**
	 * <p>
	 * 由上级 合并容器/状态context, 汇总其中所有的下属 合并容器 的状态, 得出结论, 最后调用此方法, 置 相应的下属 合并容器 的 合并状态 为 normal.
	 * </p>
	 *
	 * @param event
	 * @throws AsnException
	 */
	public void doMergeCompleteNormally(CdrProcessingEvent event) throws AsnException {

		AsnValue mergingResultStub = getMergingResultStub();

		AsnEnumerated consolidationResult = BerCodingUtils.createAsn1Value(AsnTypes.ENUMERATED);
		consolidationResult.setValue(ConsolidationResult.NORMAL.getValue());
		consolidationResult.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Primitive, 101));

		BerCodingUtils.replaceElementByTagNum((AsnContainerValueBase) mergingResultStub,
				consolidationResult, 101);
	}

	/**
	 * <p>
	 * 由上级 合并容器/状态context, 汇总其中所有的下属 合并容器 的状态, 得出结论, 最后调用此方法, 置 相应的下属 合并容器 的 合并状态 为 abnormal.
	 * </p>
	 *
	 * @param event
	 * @throws AsnException
	 */
	public void doMergeCompleteAbnormally(CdrProcessingEvent event) throws AsnException {

		AsnValue mergingResultStub = getMergingResultStub();

		AsnEnumerated consolidationResult = BerCodingUtils.createAsn1Value(AsnTypes.ENUMERATED);
		consolidationResult.setValue(ConsolidationResult.ABNORMAL.getValue());
		consolidationResult.setTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Primitive, 101));

		BerCodingUtils.replaceElementByTagNum((AsnContainerValueBase) mergingResultStub,
				consolidationResult, 101);
	}

	/**
	 * TODO: 当前实现: 当接收到丢失的部分话单后, 不进行"插入式"的增量合并操作, 而是reset已得到的合并结果, 然后重新调用一遍doMerge().
	 *
	 * @throws Exception
	 */
	public void resetMergingResult() throws Exception {

		alreadyCalledDoMerge = false;

		mergingResultStub = MergingUtils.createOutputPgwCdrFromInputPgwCdr(
				(AsnContainerValueBase) getOriginalInputPgwCdrs().get(0).getInputPgwCdr(),
				ConsolidationResult.INIT.getValue());
		consecutive = true;
		receivedRSNs = new HashSet<Long>();
		missingRSNs = new HashSet<Long>();
		minRecordSequenceNumber = Long.MAX_VALUE;
		maxRecordSequenceNumber = Long.MIN_VALUE;
	}

	// external resource usage: persistence request:

	public void requestPersistence() {

		PgwCdrMerger merger = getContext().getMerger();

		// 提交持久化请求: 合并结果CDR
		Map<String, String> mergingResultConfig = new HashMap<String, String>();
		mergingResultConfig.put("type", "P-GW");
		mergingResultConfig.put("merge", "MERGE");

		log.trace("requesting persistence (MERGED P-GW CDR): {}.", getMergingResultStub());
		merger.persist(getMergingResultStub(), mergingResultConfig);

		// 提交持久化请求: 原始话单CDR
		int oriCdrSeq = 1;
		for (InputPgwCdrObject each : getOriginalInputPgwCdrs()) {
			Map<String, String> oriCdrConfig = new HashMap<String, String>();
			oriCdrConfig.put("type", "P-GW");
			oriCdrConfig.put("merge", "ORIGINAL");
			oriCdrConfig.put("seq", Integer.valueOf(oriCdrSeq++).toString());

			log.trace("requesting persistence (ORIGINAL P-GW CDR): {}.", each);
			merger.persist(each.getInputPgwCdr(), oriCdrConfig);
		}
	}

	// exposed utilities:

	/**
	 * <p>
	 * 根据 recordSequenceNumber, 判断是否已存在相应的 P-GW CDR.
	 * </p>
	 *
	 * @param inputPgwCdr
	 * @return
	 */
	public boolean alreadyExistPgwCdr(InputPgwCdrObject inputPgwCdr) {

		boolean result = false;
		for (InputPgwCdrObject each : getOriginalInputPgwCdrs()) {
			if (each.getRecordSequenceNumber() == inputPgwCdr.getRecordSequenceNumber()) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * <p>
	 * 1. 判断: 若 originalInputPgwCdrs: List&lt;InputPgwCdrObject&gt; 中已存在相同 recordSequenceNumber 的,
	 * 则不添加;<br />
	 * <br />
	 * 2. 加入到 originalInputPgwCdrs: List&lt;InputPgwCdrObject&gt; 中去;<br />
	 * <br />
	 * 3. ++receivedOriCdrNum;<br />
	 * <br />
	 * 4. receivedRSNs: Set&lt;Long&gt; 中加入这条已添加的 recordSequenceNumber.
	 * </p>
	 *
	 * @param inputPgwCdr
	 * @return
	 */
	public boolean addOriginalPgwCdr(InputPgwCdrObject inputPgwCdr) {

		if (!alreadyExistPgwCdr(inputPgwCdr)) {

			originalInputPgwCdrs.add(inputPgwCdr);
			++receivedOriCdrNum;
			receivedRSNs.add(inputPgwCdr.getRecordSequenceNumber());

			log.trace("an original P-GW CDR has just been added to the PgwCdrMergingContainer: {}", this);

			return true;
		} else {

			return false;
		}
	}

	// getter/setters:

	/**
	 * @return the mergingResultStub
	 */
	public AsnValue getMergingResultStub() {

		return mergingResultStub;
	}

	/**
	 * @return the originalInputPgwCdrs
	 */
	public List<InputPgwCdrObject> getOriginalInputPgwCdrs() {

		return originalInputPgwCdrs;
	}

	/**
	 * @return the consecutive
	 */
	public boolean isConsecutive() {
		return consecutive;
	}

	/**
	 * @return the receivedRSNs
	 */
	public Set<Long> getReceivedRSNs() {
		return receivedRSNs;
	}

	/**
	 * @return the receivedOriCdrNum
	 */
	public int getReceivedOriCdrNum() {
		return receivedOriCdrNum;
	}

	/**
	 * @return the missingRSNs
	 */
	public Set<Long> getMissingRSNs() {
		return missingRSNs;
	}

	/**
	 * @return the key
	 */
	public PgwCdrMergingKey getKey() {

		return key;
	}

	/**
	 * @return the currentState
	 */
	public PgwCdrMergingContainerState getCurrentState() {

		return currentState;
	}

	/**
	 *
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(PgwCdrMergingContainerState currentState) {

		this.currentState = currentState;
	}

	/**
	 * @return the context
	 */
	public PgwCdrMergerContext getContext() {

		return context;
	}

	/**
	 * @return the minRecordSequenceNumber
	 */
	public long getMinRecordSequenceNumber() {

		return minRecordSequenceNumber;
	}

	/**
	 * @param minRecordSequenceNumber
	 *            the minRecordSequenceNumber to set
	 */
	protected void setMinRecordSequenceNumber(long minRecordSequenceNumber) {

		this.minRecordSequenceNumber = minRecordSequenceNumber;
	}

	/**
	 * @return the maxRecordSequenceNumber
	 */
	public long getMaxRecordSequenceNumber() {

		return maxRecordSequenceNumber;
	}

	/**
	 * @param maxRecordSequenceNumber
	 *            the maxRecordSequenceNumber to set
	 */
	protected void setMaxRecordSequenceNumber(long maxRecordSequenceNumber) {

		this.maxRecordSequenceNumber = maxRecordSequenceNumber;
	}

	public boolean isNeedMerge() {

		return needMerge;
	}
}
