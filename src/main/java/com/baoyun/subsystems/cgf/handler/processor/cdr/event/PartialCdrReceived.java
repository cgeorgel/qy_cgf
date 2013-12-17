package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;

import ch.panter.li.bi.asn.AsnValue;

public class PartialCdrReceived extends CdrReceived {

	public PartialCdrReceived(AsnValue cdr) {

		super(cdr);
	}

	public PartialCdrReceived(InputPgwCdrObject cdr) {

		super(cdr);
	}
	public PartialCdrReceived(InputSgwCdrObject cdr) {

		super(cdr);
	}
}
