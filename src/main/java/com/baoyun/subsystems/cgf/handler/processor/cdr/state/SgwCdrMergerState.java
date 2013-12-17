package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

public enum SgwCdrMergerState {
	
	NULL_STATE,

	INITIAL,

	COMPLETE_CDR_RECEIVED,

	RECEIVING,
	
	SGW_CHANGE,

	TERMINATE_PARTIAL_CDR_RECEIVED,
	
	WAITING_FOR_ANOTHER_SGW_CDR,

	WAITING_FOR_PARTICULAR_ABNORMAL_PARTIAL_CDR,

	ALL_ABNORMAL_PARTIAL_CDR_RECEIVED,

	MERGE_DONE_NORMALLY,

	PERSISTING,

	PERSIST_REQUEST_SUBMITTED,

	PERSIST_DONE_NORMALLY,

	PERSIST_DONE_ABNORMALLY,

	TIME_OUT_HANDLED,

	UNIGNORABLE_ERROR_OCCURED;

	private Class<MergerStateHandler> stateHandler;

	/**
	 * @return the stateHandler
	 */
	public Class<MergerStateHandler> getStateHandler() {
		return stateHandler;
	}

	private SgwCdrMergerState() {

	}
}
