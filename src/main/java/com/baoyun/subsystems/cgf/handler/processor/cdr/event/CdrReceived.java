package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

public class CdrReceived extends CdrProcessingEvent {

	public CdrReceived(AsnValue cdr) {

		super(cdr);
	}

	public CdrReceived(InputPgwCdrObject inputPgwCdr) {

		super(inputPgwCdr);
	}
	
	public CdrReceived(InputSgwCdrObject inputSgwCdr) {

		super(inputSgwCdr);
	}
}
