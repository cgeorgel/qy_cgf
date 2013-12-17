package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwSustainableStateHandler;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class SgwCdrWaitingForLostStateHandler extends
		SgwSustainableStateHandler {

	private static Logger log = LoggerFactory.getLogger(SgwCdrWaitingForLostStateHandler.class);

	public SgwCdrWaitingForLostStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.WAITING_FOR_PARTICULAR_ABNORMAL_PARTIAL_CDR;
	}

	@Override
	protected boolean doFilter(CdrProcessingEvent event) {

		InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();
		long recordSequenceNumber = inputSgwCdr.getRecordSequenceNumber();

		if (getContext().getMissingSeqNums().contains(recordSequenceNumber)) {

			return false;
		} else {

			return true;
		}
	}

	@Override
	protected void doHandleLogic(CdrProcessingEvent event) {

		InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();
		SgwCdrMergingKey key = inputSgwCdr.genSgwCdrMergingKey();

		SgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(key);

		if (mergingContainer == null) {
			log.error(
					"current state: {}, and a S-GW CDR: {} is just received, but the SgwCdrMergingContainer: {} does NOT exist! So the incoming S-GW CDR is ignored!",
					new Object[] { getTheState(), inputSgwCdr, key });

		} else {

			mergingContainer.addOriginalSgwCdr(inputSgwCdr);

			long recordSequenceNumber = inputSgwCdr.getRecordSequenceNumber();
			getContext().getMissingSeqNums().remove(recordSequenceNumber);

			try {
				// TODO: 改为在切换到 ALL_ABNORMAL_PARTIAL_CDR_RECEIVED 状态之前的时刻, 仅执行一次.
				mergingContainer.resetMergingResult();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(
						"errors occur while reseting merging result in SgwCdrMergingContainer: {}, exception: {}.",
						mergingContainer, MiscUtils.exceptionStackTrace2String(e));
			}
		}
	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {

		if (getContext().getMissingSeqNums().isEmpty()) {

			return SgwCdrMergerState.ALL_ABNORMAL_PARTIAL_CDR_RECEIVED;
		} else {

			return theState;
		}
	}

}
