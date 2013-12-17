package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;

import ch.panter.li.bi.asn.AsnValue;

public class TerminatePgwCdrReceived extends ParticularCdrReceived {

	public TerminatePgwCdrReceived(AsnValue inputCdr) {

		super(inputCdr);
	}

	public TerminatePgwCdrReceived(InputPgwCdrObject inputPgwCdr) {

		super(inputPgwCdr);
	}
}
