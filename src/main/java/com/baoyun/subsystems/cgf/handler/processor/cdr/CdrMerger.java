package com.baoyun.subsystems.cgf.handler.processor.cdr;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

import ch.panter.li.bi.asn.AsnValue;

/**
 * <p>
 * role:
 * </p>
 *
 * 
 *
 */
public abstract class CdrMerger {

	/**
	 *
	 * @param inputCdr
	 * @return 若是重单, 则返回true; 否则返回false.
	 * @throws Exception
	 */
	public abstract boolean checkDuplicate(AsnValue inputCdr) throws Exception;

	public abstract boolean processReceivedCdr(CdrProcessingEvent event) throws Exception;

	public abstract boolean merge(AsnValue inputCdr) throws Exception;

	public abstract CdrMergingManager getCdrMergingManager();
}
