package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.TerminatePgwCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SustainableStateHandler;

public class PgwCdrReceivingStateHandler extends SustainableStateHandler {

	private static Logger log = LoggerFactory.getLogger(PgwCdrReceivingStateHandler.class);

	public PgwCdrReceivingStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.RECEIVING;
	}

	@Override
	protected void doEnterState(CdrProcessingEvent event) {

		if (event instanceof CdrReceived) {

			InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();
			PgwCdrMergingKey key = inputPgwCdr.genPgwCdrMergingKey();

			PgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(key);
			if (mergingContainer == null) {
				log.error(
						"current state: {}, and a P-GW CDR: {} is just received, but the PgwCdrMergingContainer: {} does NOT exist! So the incoming P-GW CDR is ignored!",
						new Object[] { getTheState(), inputPgwCdr, key });
			}

			mergingContainer.action(event);
		}
	}

	@Override
	protected boolean doFilter(CdrProcessingEvent event) {

		boolean result = false;

		InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();
		PgwCdrMergingKey key = inputPgwCdr.genPgwCdrMergingKey();

		PgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(key);

		if (mergingContainer == null) {
			// @formatter:off
			/*log.error(
					"current state: {}, and a P-GW CDR: {} is just received, but the PgwCdrMergingContainer: {} does NOT exist! So the incoming P-GW CDR is ignored!",
					new Object[] { getTheState(), inputPgwCdr, key });*/
			// @formatter:on
			PgwCdrMergingContainer newMergingContainer = new PgwCdrMergingContainer(inputPgwCdr, getContext());
			getContext().getMergingContainers().put(inputPgwCdr.genPgwCdrMergingKey(), newMergingContainer);

			result = false;
		} else {

			// 根据recordSequenceNumber, 检查是否为重单.
			result = mergingContainer.alreadyExistPgwCdr(inputPgwCdr);
		}

		if (result) {
			log.info("a duplicate incoming P-GW CDR is detected: {}, ignored...", inputPgwCdr);
		}

		return result;
	}

	@Override
	protected void doHandleLogic(CdrProcessingEvent event) {

		InputPgwCdrObject inputPgwCdr = (InputPgwCdrObject) event.getArg();

		PgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(
				inputPgwCdr.genPgwCdrMergingKey());

		mergingContainer.action(event);
	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {

		if (event instanceof CdrReceived) {
			if (event instanceof TerminatePgwCdrReceived) {

				return PgwCdrMergerState.TERMINATE_PARTIAL_CDR_RECEIVED;
			} else {

				return PgwCdrMergerState.RECEIVING;
			}
		} else {
			log.error("error CdrProcessingEvent received: {}, current state: {}, context: {}",
					new Object[] { event, getTheState(), getContext() });
			return PgwCdrMergerState.RECEIVING;
		}
	}
}
