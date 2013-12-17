package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

public class TerminateSgwCdrReceived extends SgwParticularCdrReceived {
	public TerminateSgwCdrReceived(AsnValue inputCdr) {

		super(inputCdr);
	}

	public TerminateSgwCdrReceived(InputSgwCdrObject inputSgwCdr) {

		super(inputSgwCdr);
	}
}
