package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.util.Map;

import ch.panter.li.bi.asn.AsnValue;

public abstract class CdrPersistenceManager {

	abstract public void persist(AsnValue cdr, Map<String, String> config);
}
