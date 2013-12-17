package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

public abstract class SgwSustainableStateHandler extends SgwCdrMergerStateHandler {

	public SgwSustainableStateHandler(SgwCdrMergerContext context) {

		super(context);
	}

	@Override
	protected final void doHandle(CdrProcessingEvent event) {

		doHandleLogic(event);

		SgwCdrMergerState nextState = getNextState(event);

		if (!nextState.equals(getContext().getCurrentState())) {
			getContext().changeState(nextState, event);
		}
	}

	protected abstract void doHandleLogic(CdrProcessingEvent event);

	@Override
	public abstract SgwCdrMergerState getNextState(CdrProcessingEvent event);
}
