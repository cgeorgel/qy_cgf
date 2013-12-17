package com.baoyun.subsystems.cgf.handler.processor.cdr;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

/**
 * 
 * 
 * 
 */
public class NoMergingCdrMerger extends CdrMerger {

	@Override
	public boolean checkDuplicate(AsnValue inputCdr) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processReceivedCdr(CdrProcessingEvent event)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean merge(AsnValue inputCdr) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CdrMergingManager getCdrMergingManager() {
		// TODO Auto-generated method stub
		return null;
	}

}
