package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;

import ch.panter.li.bi.asn.AsnValue;

public class CompletePgwCdrReceived extends CdrReceived {

	public CompletePgwCdrReceived(AsnValue inputPgwCdr) {

		super(inputPgwCdr);
	}

	public CompletePgwCdrReceived(InputPgwCdrObject inputPgwCdr) {

		super(inputPgwCdr);
	}
}
