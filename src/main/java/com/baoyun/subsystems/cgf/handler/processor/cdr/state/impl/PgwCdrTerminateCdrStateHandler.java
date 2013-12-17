package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.InstantaneousStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.utils.MergingUtils;

public class PgwCdrTerminateCdrStateHandler extends InstantaneousStateHandler {

	private static Logger log = LoggerFactory.getLogger(PgwCdrTerminateCdrStateHandler.class);

	public PgwCdrTerminateCdrStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.TERMINATE_PARTIAL_CDR_RECEIVED;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		// 合并动作已由 PgwCdrMergingContainer 在上一个 state handler 中完成
		// @formatter:off
		/*InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();

		PgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(
				inputPgwCdr.genPgwCdrMergingKey());

		mergingContainer.action(event);*/
		// @formatter:on
	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {

		List<Long> allReceivedRSNs = new ArrayList<Long>();

		// 对各合并列表容器进行汇总, 检查是否有部分话单的丢失:
		long minRecordSequenceNumber = Long.MAX_VALUE;
		long maxRecordSequenceNumber = Long.MIN_VALUE;

		int totalPartialPgwCdrReceived = 0;

		for (PgwCdrMergingContainer each : getContext().getMergingContainers().values()) {

			if (each.getMinRecordSequenceNumber() < minRecordSequenceNumber) {
				minRecordSequenceNumber = each.getMinRecordSequenceNumber();
			}

			if (each.getMaxRecordSequenceNumber() > maxRecordSequenceNumber) {
				maxRecordSequenceNumber = each.getMaxRecordSequenceNumber();
			}

			totalPartialPgwCdrReceived += each.getReceivedOriCdrNum();
			allReceivedRSNs.addAll(each.getReceivedRSNs());

			log.trace("PgwCdrMergingContainer: {}.", each);
		}

		log.trace(
				"minRecordSequenceNumber: {}, maxRecordSequenceNumber: {}, totalPartialPgwCdrReceived: {}, PgwCdrMergerContext: {}.",
				new Object[] { minRecordSequenceNumber, maxRecordSequenceNumber,
						totalPartialPgwCdrReceived, getContext() });

		if (totalPartialPgwCdrReceived == maxRecordSequenceNumber) {
			// 所有MergingContainer中的部分话单总数, 与最大的recordSequenceNumber相同, 说明没有丢失.

			return PgwCdrMergerState.MERGE_DONE_NORMALLY;
		} else if (totalPartialPgwCdrReceived < maxRecordSequenceNumber) {

			// 存在部分话单的丢失
			List<Long> missingRSNs = MergingUtils.findMissingSequenceNumbers(allReceivedRSNs);
			getContext().getMissingSeqNums().addAll(missingRSNs);
			return PgwCdrMergerState.WAITING_FOR_PARTICULAR_ABNORMAL_PARTIAL_CDR;
		} else {
			log.error(
					"state error? totalPartialPgwCdrReceived(={}) > maxRecordSequenceNumber(={}). current state: {}.",
					new Object[] { totalPartialPgwCdrReceived, maxRecordSequenceNumber,
							getTheState() });
		}

		return null;
	}
}
