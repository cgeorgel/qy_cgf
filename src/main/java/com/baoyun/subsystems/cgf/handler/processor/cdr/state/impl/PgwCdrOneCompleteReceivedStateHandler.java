package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.InstantaneousStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class PgwCdrOneCompleteReceivedStateHandler extends InstantaneousStateHandler {

	private static Logger log = LoggerFactory
			.getLogger(PgwCdrOneCompleteReceivedStateHandler.class);

	public PgwCdrOneCompleteReceivedStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.COMPLETE_CDR_RECEIVED;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();
		PgwCdrMergingKey key = inputPgwCdr.genPgwCdrMergingKey();

		try {
			getContext().getMergingContainers().get(key).action(event);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("errors occur: {}.", MiscUtils.exceptionStackTrace2String(e));
		}
	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {

		return PgwCdrMergerState.PERSISTING;
	}
}
