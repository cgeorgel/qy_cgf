package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrCategoryKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerStateHandler;

public class SgwCdrPersistReqSubmittedStateHandler extends
		SgwCdrMergerStateHandler {
	private static Logger log = LoggerFactory
			.getLogger(SgwCdrPersistReqSubmittedStateHandler.class);

	public SgwCdrPersistReqSubmittedStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.PERSIST_REQUEST_SUBMITTED;
	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {

		return SgwCdrMergerState.PERSIST_REQUEST_SUBMITTED;
	}

	@Override
	public void enterState(CdrProcessingEvent event) {

		super.enterState(event);

		InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();
		SgwCdrCategoryKey key = inputSgwCdr.genSgwCdrCategoryKey();

		getContext().getMerger().clearCdrMergerContext(key);

		log.info("terminate state reached, context cleared. state: {}, context: {}.",
				getTheState(), getContext());
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

	}
}
