package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnException;

import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwInstantaneousStateHandler;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class SgwCdrMergeDoneNormallyStateHandler extends
		SgwInstantaneousStateHandler {

	private static Logger log = LoggerFactory.getLogger(SgwCdrMergeDoneNormallyStateHandler.class);

	public SgwCdrMergeDoneNormallyStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.MERGE_DONE_NORMALLY;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		for (SgwCdrMergingContainer each : getContext().getMergingContainers().values()) {
			try {
				each.doMergeCompleteNormally(event);
			} catch (AsnException e) {
				// TODO Auto-generated catch block
				log.error("errors occur while setting all container merge-done, exception: {}.",
						MiscUtils.exceptionStackTrace2String(e));
			}
		}
	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		return SgwCdrMergerState.PERSISTING;
	}

}
