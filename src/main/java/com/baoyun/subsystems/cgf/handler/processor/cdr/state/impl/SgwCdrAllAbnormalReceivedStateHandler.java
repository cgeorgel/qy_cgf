package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwInstantaneousStateHandler;

public class SgwCdrAllAbnormalReceivedStateHandler extends
		SgwInstantaneousStateHandler {
	public SgwCdrAllAbnormalReceivedStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.ALL_ABNORMAL_PARTIAL_CDR_RECEIVED;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {
		// FIXME Auto-generated method stub

	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		return SgwCdrMergerState.PERSISTING;
	}
}
