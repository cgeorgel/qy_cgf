package com.baoyun.subsystems.cgf.handler.processor.cdr;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

/**
 * <p>
 * 维护多个CdrMerger, 根据接收的CDR类型: {P-GW CDR, S-GW CDR, S-CDR, ...}, 委派相应的CdrMerger进行处理.
 * </p>
 *
 * 
 *
 */
public abstract class CdrMergingManager {

	@Deprecated
	abstract public int totalCdrs();

	@Deprecated
	abstract long getNextWriteBackTime();

	@Deprecated
	abstract void writeBack() throws Exception;

	public abstract void respond(CdrProcessingEvent event) throws Exception;
}
