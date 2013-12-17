package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnException;

import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.InstantaneousStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class PgwCdrMergeDoneNormallyStateHandler extends InstantaneousStateHandler {

	private static Logger log = LoggerFactory.getLogger(PgwCdrMergeDoneNormallyStateHandler.class);

	public PgwCdrMergeDoneNormallyStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.MERGE_DONE_NORMALLY;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		for (PgwCdrMergingContainer each : getContext().getMergingContainers().values()) {
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
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		return PgwCdrMergerState.PERSISTING;
	}

}
