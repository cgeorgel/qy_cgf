package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;

import ch.panter.li.bi.asn.AsnValue;

public class ParticularCdrReceived extends CdrReceived {

	public ParticularCdrReceived(AsnValue inputCdr) {

		super(inputCdr);
	}

	public ParticularCdrReceived(InputPgwCdrObject inputPgwCdr) {

		super(inputPgwCdr);
	}
}
