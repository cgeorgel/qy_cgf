package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerStateHandler;

public class SgwCdrNullStateHandler extends SgwCdrMergerStateHandler {

	public SgwCdrNullStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.NULL_STATE;
	}

	@Override
	public void doHandle(CdrProcessingEvent event) {

		getContext().changeState(getNextState(event), event);
	}

	/**
	 * Suppress the state entering log by super class.
	 */
	@Override
	public void enterState(CdrProcessingEvent event) {

	}

	/**
	 * Suppress the state entering log by super class.
	 */
	@Override
	public void exitState(CdrProcessingEvent event) {

	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {

		return SgwCdrMergerState.INITIAL;
	}
}
