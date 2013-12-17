package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

/**
 *
 * TODO: 1. MergerContext的状态, 与其下属的MergingContainer的状态的关系;<br />
 * <br />
 * TODO: 2. MergerContext何时需要直接修改其下属MergingContainer的状态? 何时不可直接修改, 而应该调用
 * 其下属MergingContainer的action(CdrProcessingEvent)?
 *
 * 
 *
 */
public enum PgwCdrMergerState {

	NULL_STATE,

	INITIAL,

	COMPLETE_CDR_RECEIVED,

	RECEIVING,

	TERMINATE_PARTIAL_CDR_RECEIVED,

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

	private PgwCdrMergerState() {

	}
}
