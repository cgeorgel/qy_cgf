package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

public enum PgwCdrMergingContainerState {

	INITIAL,

	RECEIVING,

	MERGE_TRIGGERING_PARTIAL_CDR_RECEIVED,
	TERMINATE_PARTIAL_CDR_RECEIVED,

	MERGING,
	MERGE_DONE,

	ONE_COMPLETE_P_GW_CDR_RECEIVED_NO_NEED_MERGING,

	PERSISTING,
	PERSIST_REQUEST_SUBMITTED,
	PERSIST_DONE_NORMALLY,
	PERSIST_DONE_ABNORMALLY,

	TIME_OUT_HANDLED,

	UNIGNORABLE_ERROR_OCCURED;
}