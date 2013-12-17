package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CompletePgwCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.EndOfMergingCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.InstantaneousStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergingContainerState;

public class PgwCdrInitialStateHandler extends InstantaneousStateHandler {

	private static Logger log = LoggerFactory.getLogger(PgwCdrInitialStateHandler.class);

	public PgwCdrInitialStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.INITIAL;
	}

	@Override
	protected void doHandle(CdrProcessingEvent event) {

		if (event instanceof CdrReceived) {

			InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();

			PgwCdrMergingContainer mergingContainer;
			mergingContainer = new PgwCdrMergingContainer(inputPgwCdr, getContext());

			// @formatter:off
			/*mergingContainer.addOriginalPgwCdr(inputPgwCdr);

			if (event instanceof CompletePgwCdrReceived) {

				mergingContainer
						.setCurrentState(PgwCdrMergingContainerState.ONE_COMPLETE_P_GW_CDR_RECEIVED_NO_NEED_MERGING);
			} else if (event instanceof EndOfMergingCdrReceived) {

				mergingContainer
						.setCurrentState(PgwCdrMergingContainerState.MERGE_TRIGGERING_PARTIAL_CDR_RECEIVED);
			} else {

				// TODO: 其他非触发类型的P-GW CDR
				mergingContainer
						.setCurrentState(PgwCdrMergingContainerState.RECEIVING);
			}*/
			// @formatter:on

			getContext().getMergingContainers().put(mergingContainer.getKey(), mergingContainer);

//			mergingContainer.action(event);
		} else {

			log.info("ignored event: {}, current state: {}, context: {}.", new Object[] { event,
					getTheState(), getContext() });
		}
	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {

		if (event instanceof CdrReceived) {
			if (event instanceof CompletePgwCdrReceived) {

				return PgwCdrMergerState.COMPLETE_CDR_RECEIVED;
			} else {

				return PgwCdrMergerState.RECEIVING;
			}
		} else {
			log.error("error CdrProcessingEvent received: {}, current state: {}, context: {}.",
					new Object[] { event, getTheState(), getContext() });

			return PgwCdrMergerState.INITIAL;
		}
	}
}
