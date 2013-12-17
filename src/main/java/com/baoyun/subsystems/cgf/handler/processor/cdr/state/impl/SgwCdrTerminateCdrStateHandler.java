package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwInstantaneousStateHandler;
import com.baoyun.subsystems.cgf.utils.MergingUtils;

public class SgwCdrTerminateCdrStateHandler extends
		SgwInstantaneousStateHandler {
	private static Logger log = LoggerFactory.getLogger(SgwCdrTerminateCdrStateHandler.class);

	public SgwCdrTerminateCdrStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.TERMINATE_PARTIAL_CDR_RECEIVED;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		// 合并动作已由 SgwCdrMergingContainer 在上一个 state handler 中完成
		// @formatter:off
		/*InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();

		SgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(
				inputSgwCdr.genSgwCdrMergingKey());

		mergingContainer.action(event);*/
		// @formatter:on
	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {

		List<Long> allReceivedRSNs = new ArrayList<Long>();

		// 对各合并列表容器进行汇总, 检查是否有部分话单的丢失:
		long minRecordSequenceNumber = Long.MAX_VALUE;
		long maxRecordSequenceNumber = Long.MIN_VALUE;

		int totalPartialSgwCdrReceived = 0;

		for (SgwCdrMergingContainer each : getContext().getMergingContainers().values()) {

			if (each.getMinRecordSequenceNumber() < minRecordSequenceNumber) {
				minRecordSequenceNumber = each.getMinRecordSequenceNumber();
			}

			if (each.getMaxRecordSequenceNumber() > maxRecordSequenceNumber) {
				maxRecordSequenceNumber = each.getMaxRecordSequenceNumber();
			}

			totalPartialSgwCdrReceived += each.getReceivedOriCdrNum();
			allReceivedRSNs.addAll(each.getReceivedRSNs());

			log.trace("SgwCdrMergingContainer: {}.", each);
		}

		log.trace(
				"minRecordSequenceNumber: {}, maxRecordSequenceNumber: {}, totalPartialSgwCdrReceived: {}, SgwCdrMergerContext: {}.",
				new Object[] { minRecordSequenceNumber, maxRecordSequenceNumber,
						totalPartialSgwCdrReceived, getContext() });

		if (totalPartialSgwCdrReceived == maxRecordSequenceNumber) {
			// 所有MergingContainer中的部分话单总数, 与最大的recordSequenceNumber相同, 说明没有丢失.

			return SgwCdrMergerState.MERGE_DONE_NORMALLY;
		} else if (totalPartialSgwCdrReceived < maxRecordSequenceNumber) {

			// 存在部分话单的丢失
			List<Long> missingRSNs = MergingUtils.findMissingSequenceNumbers(allReceivedRSNs);
			getContext().getMissingSeqNums().addAll(missingRSNs);
			return SgwCdrMergerState.WAITING_FOR_PARTICULAR_ABNORMAL_PARTIAL_CDR;
		} else {
			log.error(
					"state error? totalPartialSgwCdrReceived(={}) > maxRecordSequenceNumber(={}). current state: {}.",
					new Object[] { totalPartialSgwCdrReceived, maxRecordSequenceNumber,
							getTheState() });
		}

		return null;
	}

}
