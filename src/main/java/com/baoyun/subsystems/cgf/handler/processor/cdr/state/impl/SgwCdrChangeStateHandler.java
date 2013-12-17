package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.TerminateSgwCdrReceived;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwSustainableStateHandler;

public class SgwCdrChangeStateHandler extends SgwSustainableStateHandler {
	private static Logger log = LoggerFactory.getLogger(SgwCdrChangeStateHandler.class);

	public SgwCdrChangeStateHandler(SgwCdrMergerContext context) {

		super(context);
		theState = SgwCdrMergerState.SGW_CHANGE;
	}

	@Override
	protected void doEnterState(CdrProcessingEvent event) {

		if (event instanceof CdrReceived) {

			InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();
			SgwCdrMergingKey key = inputSgwCdr.genSgwCdrMergingKey();

			SgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(key);
			if (mergingContainer == null) {
				log.error(
						"current state: {}, and a S-GW CDR: {} is just received, but the SgwCdrMergingContainer: {} does NOT exist! So the incoming S-GW CDR is ignored!",
						new Object[] { getTheState(), inputSgwCdr, key });
			}

			mergingContainer.action(event);
		}
	}

	@Override
	protected boolean doFilter(CdrProcessingEvent event) {

		boolean result = false;

		InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();
		SgwCdrMergingKey key = inputSgwCdr.genSgwCdrMergingKey();

		SgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(key);

		if (mergingContainer == null) {
			
			
			SgwCdrMergingContainer newMergingContainer = new SgwCdrMergingContainer(inputSgwCdr, getContext());
			getContext().getMergingContainers().put(inputSgwCdr.genSgwCdrMergingKey(), newMergingContainer);

			result = false;
		} else {

			// 根据recordSequenceNumber, 检查是否为重单.
			result = mergingContainer.alreadyExistSgwCdr(inputSgwCdr);
		}

		if (result) {
			log.info("a duplicate incoming S-GW CDR is detected: {}, ignored...", inputSgwCdr);
		}

		return result;
	}

	@Override
	protected void doHandleLogic(CdrProcessingEvent event) {

		InputSgwCdrObject inputSgwCdr = (InputSgwCdrObject) event.getArg();

		SgwCdrMergingContainer mergingContainer = getContext().getMergingContainers().get(
				inputSgwCdr.genSgwCdrMergingKey());

		mergingContainer.action(event);
	}

	@Override
	public SgwCdrMergerState getNextState(CdrProcessingEvent event) {

		if (event instanceof CdrReceived) {
			if (event instanceof TerminateSgwCdrReceived) {

				return SgwCdrMergerState.TERMINATE_PARTIAL_CDR_RECEIVED;
			} else {

				return SgwCdrMergerState.RECEIVING;
			}
		} else {
			log.error("error CdrProcessingEvent received: {}, current state: {}, context: {}",
					new Object[] { event, getTheState(), getContext() });
			return SgwCdrMergerState.RECEIVING;
		}
	}
}
