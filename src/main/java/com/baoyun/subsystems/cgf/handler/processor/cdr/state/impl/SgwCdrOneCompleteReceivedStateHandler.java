package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwInstantaneousStateHandler;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class SgwCdrOneCompleteReceivedStateHandler extends
		SgwInstantaneousStateHandler {

	private static Logger log = LoggerFactory
			.getLogger(SgwCdrOneCompleteReceivedStateHandler.class);

	public SgwCdrOneCompleteReceivedStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.COMPLETE_CDR_RECEIVED;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();
		SgwCdrMergingKey key = inputSgwCdr.genSgwCdrMergingKey();

		try {
			getContext().getMergingContainers().get(key).action(event);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("errors occur: {}.", MiscUtils.exceptionStackTrace2String(e));
		}
	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {

		return SgwCdrMergerState.PERSISTING;
	}
}
