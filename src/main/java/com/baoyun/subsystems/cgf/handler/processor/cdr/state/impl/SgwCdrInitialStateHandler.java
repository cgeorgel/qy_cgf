package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrReceived;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CompletePgwCdrReceived;

import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwInstantaneousStateHandler;

public class SgwCdrInitialStateHandler extends SgwInstantaneousStateHandler {

	private static Logger log = LoggerFactory.getLogger(SgwCdrInitialStateHandler.class);
	
	public SgwCdrInitialStateHandler(SgwCdrMergerContext context) {
		super(context);
		theState = SgwCdrMergerState.INITIAL;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		if (event instanceof CdrReceived) {

			InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();

			SgwCdrMergingContainer mergingContainer;
			mergingContainer = new SgwCdrMergingContainer(inputSgwCdr, getContext());
			getContext().getMergingContainers().put(mergingContainer.getKey(), mergingContainer);

		} else {

			log.info("ignored event: {}, current state: {}, context: {}.", new Object[] { event,
					getTheState(), getContext() });
		}
	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {
		if (event instanceof CdrReceived) {
			if (event instanceof CompletePgwCdrReceived) {

				return SgwCdrMergerState.COMPLETE_CDR_RECEIVED;
			} else {

				return SgwCdrMergerState.RECEIVING;
			}
		} else {
			log.error("error CdrProcessingEvent received: {}, current state: {}, context: {}.",
					new Object[] { event, getTheState(), getContext() });

			return SgwCdrMergerState.INITIAL;
		}
	}

}
