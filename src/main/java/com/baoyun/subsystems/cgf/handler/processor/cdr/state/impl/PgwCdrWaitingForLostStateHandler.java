package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SustainableStateHandler;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class PgwCdrWaitingForLostStateHandler extends SustainableStateHandler {

	private static Logger log = LoggerFactory.getLogger(PgwCdrWaitingForLostStateHandler.class);

	public PgwCdrWaitingForLostStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.WAITING_FOR_PARTICULAR_ABNORMAL_PARTIAL_CDR;
	}

	@Override
	protected boolean doFilter(CdrProcessingEvent event) {

		InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();
		long recordSequenceNumber = inputPgwCdr.getRecordSequenceNumber();

		if (getContext().getMissingSeqNums().contains(recordSequenceNumber)) {

			return false;
		} else {

			return true;
		}
	}

	@Override
	protected void doHandleLogic(CdrProcessingEvent event) {

		InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();
		PgwCdrMergingKey key = inputPgwCdr.genPgwCdrMergingKey();

		PgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(key);

		if (mergingContainer == null) {
			log.error(
					"current state: {}, and a P-GW CDR: {} is just received, but the PgwCdrMergingContainer: {} does NOT exist! So the incoming P-GW CDR is ignored!",
					new Object[] { getTheState(), inputPgwCdr, key });

		} else {

			mergingContainer.addOriginalPgwCdr(inputPgwCdr);

			long recordSequenceNumber = inputPgwCdr.getRecordSequenceNumber();
			getContext().getMissingSeqNums().remove(recordSequenceNumber);

			try {
				// TODO: 改为在切换到 ALL_ABNORMAL_PARTIAL_CDR_RECEIVED 状态之前的时刻, 仅执行一次.
				mergingContainer.resetMergingResult();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(
						"errors occur while reseting merging result in PgwCdrMergingContainer: {}, exception: {}.",
						mergingContainer, MiscUtils.exceptionStackTrace2String(e));
			}
		}
	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {

		if (getContext().getMissingSeqNums().isEmpty()) {

			return PgwCdrMergerState.ALL_ABNORMAL_PARTIAL_CDR_RECEIVED;
		} else {

			return theState;
		}
	}

}
