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

import com.baoyun.subsystems.cgf.asn1.TimeStamp;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;


import com.baoyun.subsystems.cgf.handler.processor.cdr.event.*;

import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergingContainerstate;

import com.baoyun.subsystems.cgf.utils.BerCodingUtils;
import com.baoyun.subsystems.cgf.utils.MergingUtils;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class SgwCdrMergingContainer {
	private Logger log = LoggerFactory.getLogger(SgwCdrMergingContainer.class);
	
	private static String toStringPattern = "%s: {originalInputSgwCdrs=%s, consecutive=%s, receivedRSNs=%s, receivedOriCdrNum=%s, missingRSNs=%s, currentState=%s, minRecordSequenceNumber=%s, maxRecordSequenceNumber=%s, needMerge=%s, alreadyCalledDoMerge=%s}";
	
	private AsnValue mergingResultStub;

	private List<InputSgwCdrObject> originalInputSgwCdrs = new ArrayList<InputSgwCdrObject>();

	private boolean consecutive = true;

	private Set<Long> receivedRSNs = new HashSet<Long>();

	private int receivedOriCdrNum = 0;

	private Set<Long> missingRSNs = new HashSet<Long>();

	private SgwCdrMergingContainerstate currentState = SgwCdrMergingContainerstate.INITIAL;

	private SgwCdrMergingKey key;

	private SgwCdrMergerContext context;

	private long minRecordSequenceNumber = Long.MAX_VALUE;

	private long maxRecordSequenceNumber = Long.MIN_VALUE;

	private boolean needMerge;

	private boolean alreadyCalledDoMerge = false;
	
	public SgwCdrMergingContainer(InputSgwCdrObject inputSgwCdr, SgwCdrMergerContext context) {

		try {
			mergingResultStub = MergingUtils.createOutputSgwCdrFromInputSgwCdr(
					(AsnContainerValueBase) inputSgwCdr.getInputSgwCdr(),
					ConsolidationResult.INIT.getValue());
		} catch (Exception e) {
			log.error(
					"errors occur while creating output S-GW CDR from input S-GW CDR: {}, exception: {}.",
					inputSgwCdr.getInputSgwCdr(), MiscUtils.exceptionStackTrace2String(e));
		}

		key = new SgwCdrMergingKey(inputSgwCdr);
		this.context = context;
		needMerge = !inputSgwCdr.isPartial();
	}
	
	@Override
	public String toString() {

		return String.format(toStringPattern, key, originalInputSgwCdrs, consecutive, receivedRSNs,
				receivedOriCdrNum, missingRSNs, currentState, minRecordSequenceNumber,
				maxRecordSequenceNumber, needMerge, alreadyCalledDoMerge);
	}
	
	/**
	 * 当SgwCdrMergingContainer状态可能会改变时, 外界应统一通过调用此方法, 触发SgwCdrMergingContainer的自行处理
	 * (当MergerContext必须直接修改其下属的MergingContainer的状态时除外).
	 */
	public void action(CdrProcessingEvent event) {

		InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();

		if (event instanceof CdrReceived) {

			addOriginalSgwCdr(inputSgwCdr);

			switch (currentState) {

				case INITIAL:

					if (event instanceof CompleteSgwCdrReceived) {

						currentState = SgwCdrMergingContainerstate.ONE_COMPLETE_S_GW_CDR_RECEIVED_NO_NEED_MERGING;

						try {
							doOneCompleteCdrMerge(event);
						} catch (Exception e) {
							// TODO: 对 doMerge() 执行的异常处理.
							currentState = SgwCdrMergingContainerstate.UNIGNORABLE_ERROR_OCCURED;
							log.error(
									"errors occur in doOneCompleteCdrMerge(), SgwCdrMergingContainer: {}, exception: {}.",
									this, MiscUtils.exceptionStackTrace2String(e));
						}

						currentState = SgwCdrMergingContainerstate.MERGE_DONE;

					} else if (event instanceof SgwParticularCdrReceived) {

						if (event instanceof SgwEndOfMergingCdrReceived) {

							currentState = SgwCdrMergingContainerstate.MERGE_TRIGGERING_PARTIAL_CDR_RECEIVED;
						} else if (event instanceof TerminateSgwCdrReceived) {

							currentState = SgwCdrMergingContainerstate.TERMINATE_PARTIAL_CDR_RECEIVED;
						} else if (event instanceof SgwChangeCdrReceived){
							currentState = SgwCdrMergingContainerstate.SGW_CHANGE_RECEIVED;
						}

						try {
							doMerge(event);
							currentState = SgwCdrMergingContainerstate.MERGE_DONE;
						} catch (Exception e) {
							// TODO: 对 doMerge() 执行的异常处理.
							currentState = SgwCdrMergingContainerstate.UNIGNORABLE_ERROR_OCCURED;
							log.error(
									"errors occur in doMerge(), SgwCdrMergingContainer: {}, exception: {}.",
									this, MiscUtils.exceptionStackTrace2String(e));
						}

					} else {

						currentState = SgwCdrMergingContainerstate.RECEIVING;
					}

					break;

				// TODO: 是否有可能接收到的第一个部分话单, 就发生了S-GW切换, RAT切换, ...?

				case RECEIVING:

					if (event instanceof SgwParticularCdrReceived) {

						// 设置 MergingContainer 的状态.
						if (event instanceof SgwEndOfMergingCdrReceived) {

							currentState = SgwCdrMergingContainerstate.MERGE_TRIGGERING_PARTIAL_CDR_RECEIVED;
						} else if (event instanceof TerminateSgwCdrReceived) {

							currentState = SgwCdrMergingContainerstate.TERMINATE_PARTIAL_CDR_RECEIVED;
						} else if (event instanceof SgwChangeCdrReceived){
							currentState = SgwCdrMergingContainerstate.SGW_CHANGE_RECEIVED;
						}

						// 对接收到的特殊部分 CDR 进行处理.
						if (event instanceof SgwEndOfMergingCdrReceived
								|| event instanceof TerminateSgwCdrReceived) {

							try {
								currentState = SgwCdrMergingContainerstate.MERGING;
								doMerge(event);
								currentState = SgwCdrMergingContainerstate.MERGE_DONE;
							} catch (Exception e) {
								// TODO: 对 doMerge() 执行的异常处理.
								currentState = SgwCdrMergingContainerstate.UNIGNORABLE_ERROR_OCCURED;
								log.error(
										"errors occur in doMerge(), SgwCdrMergingContainer: {}, exception: {}.",
										this, MiscUtils.exceptionStackTrace2String(e));
							}
						}
					}

					break;

				default:
					log.warn(
							"state error? inappropriate invocation? currentState: {}, SgwCdrMergingContainer: {}.",
							currentState, this);
			}
		}
	}
	

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
				 * 1. 按recordSequenceNumber, 对PendingList中所有的S-GW CDR进行排序:
				 */
				List<InputSgwCdrObject> originalInputSgwCdrs = getOriginalInputSgwCdrs();
				Collections.sort(originalInputSgwCdrs);

				AsnValue firstPartialSgwCdr = originalInputSgwCdrs.get(0).getInputSgwCdr();
				AsnValue lastPartialSgwCdr = originalInputSgwCdrs.get(originalInputSgwCdrs.size() - 1)
						.getInputSgwCdr();
				// TODO: refactor to use InputSgwCdrObject:
				setMinRecordSequenceNumber(MergingUtils
						.getRecordSequenceNumberValueOfInputCdr(firstPartialSgwCdr).intValue());
				setMaxRecordSequenceNumber(MergingUtils
						.getRecordSequenceNumberValueOfInputCdr(lastPartialSgwCdr).intValue());

				/*
				 * 2. 处理合并结果的recordOpeningTime 和 duration字段:
				 */

				// 话单是否连续: 根据recordOpeningTime + duration与下一个部分话单的recordOpeningTime是否相差2秒以内判断:
				boolean isConsecutive = true;
				// 部分话单的duration累计值: TODO: 使用int类型是否够大?
				int totalDuration = MergingUtils.getDurationValueOfInputCdr(firstPartialSgwCdr)
						.intValue();

				AsnValue previousPartialSgwCdr = firstPartialSgwCdr;
				for (int i = 1; i < originalInputSgwCdrs.size(); ++i) {

					receivedRSNs.add(originalInputSgwCdrs.get(i).getRecordSequenceNumber());

					AsnValue currentPartialSgwCdr = originalInputSgwCdrs.get(i).getInputSgwCdr();
					totalDuration += MergingUtils
							.getDurationValueOfInputCdr(currentPartialSgwCdr).intValue();

					if (!isConsecutive
							|| MergingUtils.getRecordSequenceNumberValueOfInputCdr(
									currentPartialSgwCdr)
									.intValue()
									- MergingUtils.getRecordSequenceNumberValueOfInputCdr(
											previousPartialSgwCdr).intValue() != 1) {
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
					 * 8.2.5. SGW-CDR的合并
					 *
					 * 4）需要过滤的字段 若部分话单连续, 则: 对于一批连续部分话单，Duration字段 = （最后的部分记录的Record Opening Time -
					 * 最先的部分记录的Record Opening Time + 最后的部分记录的Duration字段）； 对于不连续话单Duration字段累加。
					 */
					// @formatter:on
					TimeStamp firstRecordOpeningTime = MergingUtils
							.getRecordOpeningTimeValueOfInputCdr(firstPartialSgwCdr);
					TimeStamp lastRecordOpeningTime = MergingUtils
							.getRecordOpeningTimeValueOfInputCdr(lastPartialSgwCdr);
					int lastDuration = MergingUtils
							.getDurationValueOfInputCdr(lastPartialSgwCdr).intValue();

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
						(AsnContainerValueBase) firstPartialSgwCdr, 13);

				if (!BerCodingUtils.getElementByTagNum((AsnContainerValueBase) mergingResultStub,
						13).equals(minRecordOpeningTime)) {
					// 当接收到的第一个部分S-GW CDR不是recordSequenceNumber最小的那个, 即CG并非按部分话单的顺序对其进行接收时:
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

				for (int i = 0; i < originalInputSgwCdrs.size(); ++i) {
					// 按recordSequenceNumber的顺序, 将每个部分话单的recordSequenceNumber,
					// 和localSequenceNumber, 分别append到合并结果的recordSequenceNumber列表,
					// 以及localSequenceNumber列表中去:

					AsnValue currentPartialSgwCdr = originalInputSgwCdrs.get(i).getInputSgwCdr();

					AsnValue currentRecordSequenceNumber = BerCodingUtils.getElementByTagNum(
							(AsnContainerValueBase) currentPartialSgwCdr, 17);
					if (currentRecordSequenceNumber != null) {
						// 对于部分话单, recordSequenceNumber应该一定非空.
						BerCodingUtils.appendElement(mergingResultRecordNumberList,
								currentRecordSequenceNumber);
					}

					AsnValue currentLocalSequenceNumber = BerCodingUtils.getElementByTagNum(
							(AsnContainerValueBase) currentPartialSgwCdr, 20);
					if (currentLocalSequenceNumber != null) {
						BerCodingUtils.appendElement(mergingResultLocalRecordNumberList,
								currentLocalSequenceNumber);
					}
				}

				// TODO: 完成有完整待合并S-GW CDR列表的PendingList的合并.

				/*
				 * 4. 处理合并结果的listOfTrafficVolumes字段(FIXME: 仅仅将所有部分话单的listOfTrafficVolumes进行连接):
				 *
				 * listOfTrafficVolumes		[12] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
				 */

				// 合并结果的listOfTrafficVolumes: SEQUENCE OF 字段:
				AsnContainerValueBase mergingResultListOfTrafficVolumes = BerCodingUtils
						.getComplexElementByTagNum((AsnContainerValueBase) mergingResultStub, 12);
				if (mergingResultListOfTrafficVolumes == null) {

					mergingResultListOfTrafficVolumes = BerCodingUtils
							.createAsn1Value(AsnTypes.SEQUENCE_OF);
					mergingResultListOfTrafficVolumes.setTag(BerCodingUtils.createAsn1Tag(
							AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 12));
				}

				for (int i = 0; i < originalInputSgwCdrs.size(); ++i) {
					// 按recordSequenceNumber的顺序, 将每个部分话单的listOfTrafficVolumes中的每条记录,
					// 都给append到合并结果的listOfTrafficVolumes中去:

					AsnValue currentPartialSgwCdr = originalInputSgwCdrs.get(i).getInputSgwCdr();

					AsnContainerValueBase currentListOfTrafficVolumes = BerCodingUtils
							.getComplexElementByTagNum(
									(AsnContainerValueBase) currentPartialSgwCdr, 12);
					if (currentListOfTrafficVolumes != null) {
						for (int j = 0; j < currentListOfTrafficVolumes.size(); ++j) {
							BerCodingUtils.appendElement(mergingResultListOfTrafficVolumes,
									BerCodingUtils
											.getElementByPosition(currentListOfTrafficVolumes, j));
						}
					}
				}

				if (BerCodingUtils.hasElementWithTagNum((AsnContainerValueBase) mergingResultStub,
						12)) {
					BerCodingUtils.replaceElementByTagNum(
							(AsnContainerValueBase) mergingResultStub,
							mergingResultListOfTrafficVolumes, 12);
				} else {
					BerCodingUtils.addElement((AsnContainerValueBase) mergingResultStub,
							mergingResultListOfTrafficVolumes);
				}

			}

			if (isNeedMerge()) {
				setCurrentState(SgwCdrMergingContainerstate.MERGE_DONE);
				log.trace("SgwCdrMergingContainer: {} state change to: {}.", getKey(),
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

		mergingResultStub = MergingUtils.createOutputSgwCdrFromInputSgwCdr(
				(AsnContainerValueBase) getOriginalInputSgwCdrs().get(0).getInputSgwCdr(),
				ConsolidationResult.INIT.getValue());
		consecutive = true;
		receivedRSNs = new HashSet<Long>();
		missingRSNs = new HashSet<Long>();
		minRecordSequenceNumber = Long.MAX_VALUE;
		maxRecordSequenceNumber = Long.MIN_VALUE;
	}

	public void requestPersistence() {

		SgwCdrMerger merger = getContext().getMerger();

		// 提交持久化请求: 合并结果CDR
		Map<String, String> mergingResultConfig = new HashMap<String, String>();
		mergingResultConfig.put("type", "S-GW");
		mergingResultConfig.put("merge", "MERGE");

		log.trace("requesting persistence (MERGED S-GW CDR): {}.", getMergingResultStub());
		merger.persist(getMergingResultStub(), mergingResultConfig);

		// 提交持久化请求: 原始话单CDR
		int oriCdrSeq = 1;
		for (InputSgwCdrObject each : getOriginalInputSgwCdrs()) {
			Map<String, String> oriCdrConfig = new HashMap<String, String>();
			oriCdrConfig.put("type", "S-GW");
			oriCdrConfig.put("merge", "ORIGINAL");
			oriCdrConfig.put("seq", Integer.valueOf(oriCdrSeq++).toString());

			log.trace("requesting persistence (ORIGINAL S-GW CDR): {}.", each);
			merger.persist(each.getInputSgwCdr(), oriCdrConfig);
		}
	}
	/**
	 * <p>
	 * 根据 recordSequenceNumber, 判断是否已存在相应的 S-GW CDR.
	 * </p>
	 *
	 * @param inputSgwCdr
	 * @return
	 */
	public boolean alreadyExistSgwCdr(InputSgwCdrObject inputSgwCdr) {

		boolean result = false;
		for (InputSgwCdrObject each : getOriginalInputSgwCdrs()) {
			if (each.getRecordSequenceNumber() == inputSgwCdr.getRecordSequenceNumber()) {
				result = true;
				break;
			}
		}

		return result;
	}
	
	/**
	 * <p>
	 * 1. 判断: 若 originalInputSgwCdrs: List&lt;InputSgwCdrObject&gt; 中已存在相同 recordSequenceNumber 的,
	 * 则不添加;<br />
	 * <br />
	 * 2. 加入到 originalInputSgwCdrs: List&lt;InputSgwCdrObject&gt; 中去;<br />
	 * <br />
	 * 3. ++receivedOriCdrNum;<br />
	 * <br />
	 * 4. receivedRSNs: Set&lt;Long&gt; 中加入这条已添加的 recordSequenceNumber.
	 * </p>
	 *
	 * @param inputSgwCdr
	 * @return
	 */
	public boolean addOriginalSgwCdr(InputSgwCdrObject inputSgwCdr) {

		if (!alreadyExistSgwCdr(inputSgwCdr)) {

			originalInputSgwCdrs.add(inputSgwCdr);
			++receivedOriCdrNum;
			receivedRSNs.add(inputSgwCdr.getRecordSequenceNumber());

			log.trace("an original S-GW CDR has just been added to the SgwCdrMergingContainer: {}", this);

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
	 * @return the originalInputSgwCdrs
	 */
	public List<InputSgwCdrObject> getOriginalInputSgwCdrs() {

		return originalInputSgwCdrs;
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
	public SgwCdrMergingKey getKey() {

		return key;
	}

	/**
	 * @return the currentState
	 */
	public SgwCdrMergingContainerstate getCurrentState() {

		return currentState;
	}

	/**
	 *
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(SgwCdrMergingContainerstate currentState) {

		this.currentState = currentState;
	}

	/**
	 * @return the context
	 */
	public SgwCdrMergerContext getContext() {

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
