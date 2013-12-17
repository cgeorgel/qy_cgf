package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrCategoryKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerStateHandler;

/**
 *
 * TODO: 这是临时的终态StateHandler.
 *
 * 
 *
 */
public class PgwCdrPersistReqSubmittedStateHandler extends PgwCdrMergerStateHandler {

	private static Logger log = LoggerFactory
			.getLogger(PgwCdrPersistReqSubmittedStateHandler.class);

	public PgwCdrPersistReqSubmittedStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.PERSIST_REQUEST_SUBMITTED;
	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {

		return PgwCdrMergerState.PERSIST_REQUEST_SUBMITTED;
	}

	@Override
	public void enterState(CdrProcessingEvent event) {

		super.enterState(event);

		InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();
		PgwCdrCategoryKey key = inputPgwCdr.genPgwCdrCategoryKey();

		getContext().getMerger().clearCdrMergerContext(key);

		log.info("terminate state reached, context cleared. state: {}, context: {}.",
				getTheState(), getContext());
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

	}
}
