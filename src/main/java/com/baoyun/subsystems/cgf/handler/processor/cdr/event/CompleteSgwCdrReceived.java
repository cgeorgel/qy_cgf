package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

import ch.panter.li.bi.asn.AsnValue;

public class CompleteSgwCdrReceived extends CdrReceived {

	public CompleteSgwCdrReceived(AsnValue cdr) {
		super(cdr);
		// TODO Auto-generated constructor stub
	}
	
	public CompleteSgwCdrReceived(InputSgwCdrObject inputSgwCdr) {

		super(inputSgwCdr);
	}

}
