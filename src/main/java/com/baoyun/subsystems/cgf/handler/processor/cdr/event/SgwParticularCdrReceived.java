package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

public class SgwParticularCdrReceived extends CdrReceived {
	public SgwParticularCdrReceived(AsnValue inputCdr) {

		super(inputCdr);
	}

	public SgwParticularCdrReceived(InputSgwCdrObject inputSgwCdr) {

		super(inputSgwCdr);
	}
}
