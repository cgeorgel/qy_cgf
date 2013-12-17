package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

public class SgwChangeCdrReceived extends SgwParticularCdrReceived {
	public SgwChangeCdrReceived(AsnValue inputCdr) {

		super(inputCdr);
	}

	public SgwChangeCdrReceived(InputSgwCdrObject inputSgwCdr) {

		super(inputSgwCdr);
	}
}
