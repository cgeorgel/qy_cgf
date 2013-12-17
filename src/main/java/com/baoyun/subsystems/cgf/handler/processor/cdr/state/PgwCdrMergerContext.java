package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrCategoryKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMerger;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.PgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrAllAbnormalReceivedStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrInitialStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrMergeDoneNormallyStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrNullStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrOneCompleteReceivedStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrPersistReqSubmittedStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrPersistingStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrReceivingStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrTerminateCdrStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.PgwCdrWaitingForLostStateHandler;

public class PgwCdrMergerContext extends CdrMergerContext {

	private static Logger log = LoggerFactory.getLogger(PgwCdrMergerContext.class);

	private final PgwCdrMerger merger;

	private PgwCdrMergerState currentState = PgwCdrMergerState.NULL_STATE;

	private Map<PgwCdrMergerState, PgwCdrMergerStateHandler> stateHandlers;

	private PgwCdrMergerStateHandler currentStateHandler;

	private final PgwCdrCategoryKey key;

	private final Date startUpTime = new Date();

	private Map<PgwCdrMergingKey, PgwCdrMergingContainer> mergingContainers = new HashMap<PgwCdrMergingKey, PgwCdrMergingContainer>();

	private List<Long> missingSeqNums = new ArrayList<Long>();

	public PgwCdrMergerContext(PgwCdrMerger merger, PgwCdrCategoryKey key) {

		this.merger = merger;
		this.key = key;

		stateHandlers = new HashMap<PgwCdrMergerState, PgwCdrMergerStateHandler>();
		stateHandlers.put(PgwCdrMergerState.NULL_STATE, new PgwCdrNullStateHandler(this));
		stateHandlers.put(PgwCdrMergerState.INITIAL, new PgwCdrInitialStateHandler(this));
		stateHandlers.put(PgwCdrMergerState.COMPLETE_CDR_RECEIVED,
				new PgwCdrOneCompleteReceivedStateHandler(this));
		stateHandlers.put(PgwCdrMergerState.RECEIVING, new PgwCdrReceivingStateHandler(this));

		stateHandlers.put(PgwCdrMergerState.TERMINATE_PARTIAL_CDR_RECEIVED,
				new PgwCdrTerminateCdrStateHandler(this));
		stateHandlers.put(PgwCdrMergerState.WAITING_FOR_PARTICULAR_ABNORMAL_PARTIAL_CDR,
				new PgwCdrWaitingForLostStateHandler(this));
		stateHandlers.put(PgwCdrMergerState.ALL_ABNORMAL_PARTIAL_CDR_RECEIVED,
				new PgwCdrAllAbnormalReceivedStateHandler(this));

		// FIXME: implement PgwCdrMergerStateHandler and config stateHandlers.
		stateHandlers.put(PgwCdrMergerState.MERGE_DONE_NORMALLY,
				new PgwCdrMergeDoneNormallyStateHandler(this));

		stateHandlers.put(PgwCdrMergerState.PERSISTING, new PgwCdrPersistingStateHandler(this));

		stateHandlers.put(PgwCdrMergerState.PERSIST_REQUEST_SUBMITTED,
				new PgwCdrPersistReqSubmittedStateHandler(this));
		stateHandlers.put(PgwCdrMergerState.PERSIST_DONE_NORMALLY, null);
		stateHandlers.put(PgwCdrMergerState.PERSIST_DONE_ABNORMALLY, null);
		stateHandlers.put(PgwCdrMergerState.TIME_OUT_HANDLED, null);
		stateHandlers.put(PgwCdrMergerState.UNIGNORABLE_ERROR_OCCURED, null);

		currentStateHandler = stateHandlers.get(PgwCdrMergerState.NULL_STATE);
	}

	@Override
	public String toString() {

		return key.toString();
	}

	private PgwCdrMergerState nextState(CdrProcessingEvent event) {

		// TODO: a table driven state changing diagram? or delegate to state handler?
		return currentStateHandler.getNextState(event);
	}

	@Override
	public void action(CdrProcessingEvent event) {

		PgwCdrMergerStateHandler handler = currentStateHandler;
		handler.handle(event);
	}

	public void changeState(PgwCdrMergerState newState, CdrProcessingEvent event) {

		if (!currentState.equals(newState)) {

			log.trace("changing state: current state: {}, next state: {}, context: {}.",
					new Object[] { currentState, newState, this });

			// TODO: (1)旧state handler的exitState(), 新state handler的entryState()的执行,
			// 与 (2)设置context的currentStateHandler和currentState, 哪个先进行?

			PgwCdrMergerStateHandler oldHandler = stateHandlers.get(getCurrentState());
			PgwCdrMergerStateHandler newHandler = stateHandlers.get(newState);

			currentStateHandler = newHandler;
			currentState = newState;

			oldHandler.exitState(event);
			newHandler.enterState(event);
		}
	}

	// getters/setters:

	public PgwCdrMerger getMerger() {

		return merger;
	}

	@Override
	public String getUniqueId() {

		return key.toString();
	}

	/**
	 * @return the currentState
	 */
	public PgwCdrMergerState getCurrentState() {

		return currentState;
	}

	public Date getStartUpTime() {

		return startUpTime;
	}

	public Map<PgwCdrMergingKey, PgwCdrMergingContainer> getMergingContainers() {

		return mergingContainers;
	}

	/**
	 * @return the missingSeqNums
	 */
	public List<Long> getMissingSeqNums() {
		return missingSeqNums;
	}
}
