package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.util.HashMap;
import java.util.Map;

import net.rubyeye.xmemcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;
import com.baoyun.subsystems.cgf.cache.CacheClientFactory;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrPersistenceRequest;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrReceived;

import com.baoyun.subsystems.cgf.utils.MergingUtils;

public class DefaultCdrMergingManager extends CdrMergingManager {

	protected enum CdrMergerName {

		P_GW_CDR_MERGER("P-GW CDR merger"),
		S_GW_CDR_MERGER("S-GW CDR merger");

		private final String name;

		CdrMergerName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private static Logger log = LoggerFactory.getLogger(DefaultCdrMergingManager.class);

	/**
	 * The storage backend.
	 */
	protected MemcachedClient client = CacheClientFactory.getCacheClient();

	protected Map<String, CdrMerger> cdrMerger = new HashMap<String, CdrMerger>();

	public DefaultCdrMergingManager() {

//		cdrMerger.put(CdrMergerName.P_GW_CDR_MERGER.getName(), new PgwCdrMerger());
		cdrMerger.put(CdrMergerName.P_GW_CDR_MERGER.getName(), new RefactoredPgwCdrMerger());
		cdrMerger.put(CdrMergerName.S_GW_CDR_MERGER.getName(), new SgwCdrMerger());
	}

	@Override
	public int totalCdrs() {
		// TODO not implemented yet.
		return 0;
	}

	@Override
	long getNextWriteBackTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void writeBack() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void respond(CdrProcessingEvent event) throws Exception {

		if (event instanceof CdrReceived) {
			Object inputCdr = event.getArg();
			
			int cdrType = 0;
			
			if (inputCdr instanceof InputPgwCdrObject)
				cdrType = ((InputPgwCdrObject) inputCdr).getRecordType();
			if (inputCdr instanceof InputSgwCdrObject)
				cdrType = ((InputSgwCdrObject) inputCdr).getRecordType();
			
			if (inputCdr instanceof InputPgwCdrObject || inputCdr instanceof InputSgwCdrObject ) {

				CdrMerger merger;
				switch (cdrType) {
					case CdrType.PGW_CDR:

						merger = cdrMerger.get(CdrMergerName.P_GW_CDR_MERGER.getName());
						if (merger != null) {
							merger.processReceivedCdr(event);
						} else {
							log.info("corresponding CdrMerger for input P-GW CDR is NOT available");
						}

						break;

					case CdrType.SGW_CDR:
						merger = cdrMerger.get(CdrMergerName.S_GW_CDR_MERGER.getName());
						if (merger != null){
							merger.processReceivedCdr(event);
						}else{
							log.info("corresponding CdrMerger for input S-GW CDR is NOT available");
						}
						break;
					default:
						log.info("unknown CDR: type={} received, ignore", cdrType);
				}
			} else {

				throw new IllegalArgumentException(
						"the attribute: arg of CdrReceived event should be an instance of Input*CdrObject, but what I've got is "
								+ inputCdr.getClass());
			}
		} else if (event instanceof CdrPersistenceRequest) {

		} 
	}
}
