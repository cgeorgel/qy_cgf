package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrCategoryKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMerger;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingContainer;
import com.baoyun.subsystems.cgf.handler.processor.cdr.SgwCdrMergingKey;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrAllAbnormalReceivedStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrChangeStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrInitialStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrMergeDoneNormallyStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrNullStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrOneCompleteReceivedStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrPersistReqSubmittedStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrPersistingStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrReceivingStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrTerminateCdrStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl.SgwCdrWaitingForLostStateHandler;


public class SgwCdrMergerContext extends CdrMergerContext {

	private static Logger log = LoggerFactory.getLogger(SgwCdrMergerContext.class);

	private final SgwCdrMerger merger;

	private SgwCdrMergerState currentState = SgwCdrMergerState.NULL_STATE;

	private Map<SgwCdrMergerState, SgwCdrMergerStateHandler> stateHandlers;

	private SgwCdrMergerStateHandler currentStateHandler;

	private final SgwCdrCategoryKey key;

	private final Date startUpTime = new Date();

	private Map<SgwCdrMergingKey, SgwCdrMergingContainer> mergingContainers = new HashMap<SgwCdrMergingKey, SgwCdrMergingContainer>();

	private List<Long> missingSeqNums = new ArrayList<Long>();
	public SgwCdrMergerContext(SgwCdrMerger merger, SgwCdrCategoryKey key) {

		this.merger = merger;
		this.key = key;

		stateHandlers = new HashMap<SgwCdrMergerState, SgwCdrMergerStateHandler>();
		stateHandlers.put(SgwCdrMergerState.NULL_STATE, new SgwCdrNullStateHandler(this));
		stateHandlers.put(SgwCdrMergerState.INITIAL, new SgwCdrInitialStateHandler(this));
		stateHandlers.put(SgwCdrMergerState.COMPLETE_CDR_RECEIVED,
				new SgwCdrOneCompleteReceivedStateHandler(this));
		stateHandlers.put(SgwCdrMergerState.RECEIVING, new SgwCdrReceivingStateHandler(this));
		stateHandlers.put(SgwCdrMergerState.SGW_CHANGE, new SgwCdrChangeStateHandler(this));
		
		stateHandlers.put(SgwCdrMergerState.TERMINATE_PARTIAL_CDR_RECEIVED,
				new SgwCdrTerminateCdrStateHandler(this));
		stateHandlers.put(SgwCdrMergerState.WAITING_FOR_PARTICULAR_ABNORMAL_PARTIAL_CDR,
				new SgwCdrWaitingForLostStateHandler(this));
		stateHandlers.put(SgwCdrMergerState.ALL_ABNORMAL_PARTIAL_CDR_RECEIVED,
				new SgwCdrAllAbnormalReceivedStateHandler(this));

		// FIXME: implement SgwCdrMergerStateHandler and config stateHandlers.
		stateHandlers.put(SgwCdrMergerState.MERGE_DONE_NORMALLY,
				new SgwCdrMergeDoneNormallyStateHandler(this));

		stateHandlers.put(SgwCdrMergerState.PERSISTING, new SgwCdrPersistingStateHandler(this));

		stateHandlers.put(SgwCdrMergerState.PERSIST_REQUEST_SUBMITTED,
				new SgwCdrPersistReqSubmittedStateHandler(this));
		stateHandlers.put(SgwCdrMergerState.PERSIST_DONE_NORMALLY, null);
		stateHandlers.put(SgwCdrMergerState.PERSIST_DONE_ABNORMALLY, null);
		stateHandlers.put(SgwCdrMergerState.TIME_OUT_HANDLED, null);
		stateHandlers.put(SgwCdrMergerState.UNIGNORABLE_ERROR_OCCURED, null);

		currentStateHandler = stateHandlers.get(SgwCdrMergerState.NULL_STATE);
	}
	
	@Override
	public String toString() {

		return key.toString();
	}
	
	private SgwCdrMergerState nextState(CdrProcessingEvent event) {

		// TODO: a table driven state changing diagram? or delegate to state handler?
		return currentStateHandler.getNextState(event);
	}
	@Override
	public void action(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		SgwCdrMergerStateHandler handler = currentStateHandler;
		handler.handle(event);
	}
	
	public void changeState(SgwCdrMergerState newState, CdrProcessingEvent event) {

		if (!currentState.equals(newState)) {

			log.trace("changing state: current state: {}, next state: {}, context: {}.",
					new Object[] { currentState, newState, this });

			
			SgwCdrMergerStateHandler oldHandler = stateHandlers.get(getCurrentState());
			SgwCdrMergerStateHandler newHandler = stateHandlers.get(newState);

			currentStateHandler = newHandler;
			currentState = newState;

			oldHandler.exitState(event);
			newHandler.enterState(event);
		}
	}

	// getters/setters:

		public SgwCdrMerger getMerger() {

			return merger;
		}

		@Override
		public String getUniqueId() {

			return key.toString();
		}

		/**
		 * @return the currentState
		 */
		public SgwCdrMergerState getCurrentState() {

			return currentState;
		}

		public Date getStartUpTime() {

			return startUpTime;
		}

		public Map<SgwCdrMergingKey, SgwCdrMergingContainer> getMergingContainers() {

			return mergingContainers;
		}

		/**
		 * @return the missingSeqNums
		 */
		public List<Long> getMissingSeqNums() {
			return missingSeqNums;
		}

}
