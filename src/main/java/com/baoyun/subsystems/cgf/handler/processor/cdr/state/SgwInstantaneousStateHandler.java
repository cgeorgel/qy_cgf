package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;


public abstract class SgwInstantaneousStateHandler extends SgwCdrMergerStateHandler {

	public SgwInstantaneousStateHandler(SgwCdrMergerContext context) {

		super(context);
	}

	@Override
	protected final void doEnterState(CdrProcessingEvent event) {

		handle(event);

		SgwCdrMergerState newState = getNextState(event);

		getContext().changeState(newState, event);
		
	}
}
