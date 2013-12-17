package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.InstantaneousStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergingContainerState;

public class PgwCdrPersistingStateHandler extends InstantaneousStateHandler {

	public PgwCdrPersistingStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.PERSISTING;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		for (PgwCdrMergingContainer eachContainer : getContext().getMergingContainers().values()) {

			eachContainer.setCurrentState(PgwCdrMergingContainerState.PERSISTING);

			eachContainer.requestPersistence();
		}
	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {

		return PgwCdrMergerState.PERSIST_REQUEST_SUBMITTED;
	}
}
