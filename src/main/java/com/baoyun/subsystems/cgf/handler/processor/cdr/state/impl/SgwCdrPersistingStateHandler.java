package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergingContainerstate;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwInstantaneousStateHandler;

public class SgwCdrPersistingStateHandler extends SgwInstantaneousStateHandler {

	public SgwCdrPersistingStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.PERSISTING;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		for (SgwCdrMergingContainer eachContainer : getContext().getMergingContainers().values()) {

			eachContainer.setCurrentState(SgwCdrMergingContainerstate.PERSISTING);

			eachContainer.requestPersistence();
		}
	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {

		return SgwCdrMergerState.PERSIST_REQUEST_SUBMITTED;
	}
}
