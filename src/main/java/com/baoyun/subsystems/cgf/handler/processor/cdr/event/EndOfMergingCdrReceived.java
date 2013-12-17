package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;

import ch.panter.li.bi.asn.AsnValue;

public class EndOfMergingCdrReceived extends ParticularCdrReceived {

	public EndOfMergingCdrReceived(AsnValue inputCdr) {

		super(inputCdr);
	}

	public EndOfMergingCdrReceived(InputPgwCdrObject inputPgwCdr) {

		super(inputPgwCdr);
	}
}
