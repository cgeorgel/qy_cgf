package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.InstantaneousStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;

public class PgwCdrAllAbnormalReceivedStateHandler extends InstantaneousStateHandler {

	public PgwCdrAllAbnormalReceivedStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.ALL_ABNORMAL_PARTIAL_CDR_RECEIVED;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {
		// FIXME Auto-generated method stub

	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		return PgwCdrMergerState.PERSISTING;
	}
}
