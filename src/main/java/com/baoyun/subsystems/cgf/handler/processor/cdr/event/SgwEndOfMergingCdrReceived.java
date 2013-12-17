package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

public class SgwEndOfMergingCdrReceived extends SgwParticularCdrReceived {
	public SgwEndOfMergingCdrReceived(AsnValue inputCdr) {

		super(inputCdr);
	}

	public SgwEndOfMergingCdrReceived(InputSgwCdrObject inputSgwCdr) {

		super(inputSgwCdr);
	}
}
