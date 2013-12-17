package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnValue;


import com.baoyun.subsystems.cgf.asn1.InputSgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;

public class SgwCdrMerger extends CdrMerger {

	private static Logger log = LoggerFactory.getLogger(SgwCdrMerger.class);
	
	protected StorageBackend<CdrMergerContext> mergerContexts;

	protected CdrPersistenceManager persistMgr;
	
	protected Map<String, String> filterConfig = new HashMap<String, String>();

	protected boolean incrementalMerging = false;
	
	
	public boolean filter(AsnValue inputCdr) {

		return false;
	}
	
	public void persist(AsnValue outputCdr, Map<String, String> config) {

		persistMgr.persist(outputCdr, config);
	}
	
	public void clearCdrMergerContext(SgwCdrCategoryKey key) {

		mergerContexts.delete(key.toString());
	}
	
	public InputSgwCdrObject genInputSgwCdrObject(AsnValue inputSgwCdr)  {

		return new InputSgwCdrObject(inputSgwCdr);
	}
	
	protected CdrMergerContext createCdrMergerContext(InputSgwCdrObject inputSgwCdr) {

		SgwCdrCategoryKey key = inputSgwCdr.genSgwCdrCategoryKey();

		SgwCdrMergerContext context = new SgwCdrMergerContext(this, key);

		return context;
	}
	
	
	@Override
	public boolean processReceivedCdr(CdrProcessingEvent event)
			throws Exception {
		boolean newCdrAdded = false;

		InputSgwCdrObject inputSgwCdr;

		Object inputCdr = event.getArg();
		if (inputCdr instanceof InputSgwCdrObject) {
			inputSgwCdr = (InputSgwCdrObject) inputCdr;
		} else {
			 inputSgwCdr = genInputSgwCdrObject((AsnValue) inputCdr);
		}

		log.trace("start processing input S-GW CDR: {}.", inputSgwCdr);
//		MiscUtils.periodicallyDumpThread(10, 1); // temp debug: dump thread later.

		SgwCdrCategoryKey key = inputSgwCdr.genSgwCdrCategoryKey();

		SgwCdrMergerContext mergerCtxt;
		if (mergerContexts.contains(key.toString())) {

			mergerCtxt = (SgwCdrMergerContext) mergerContexts.get(key.toString());
			mergerCtxt.action(event);
		} else {
			// 某个key的S-GW CDR的首次接收: 创建相应的状态机context(刚创建完其状态为NULL_STATE), 并触发状态机的启动.
			mergerCtxt = (SgwCdrMergerContext) createCdrMergerContext(inputSgwCdr);
			mergerContexts.put(key.toString(), mergerCtxt);
			mergerCtxt.action(event);
		}

		return newCdrAdded;
	}
	
	@Override
	public boolean checkDuplicate(AsnValue inputCdr) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean merge(AsnValue inputCdr) throws Exception {
		// TODO Auto-generated method stub
		boolean newCdrAdded = false;

		return newCdrAdded;
	}

	@Override
	public CdrMergingManager getCdrMergingManager() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SgwCdrMerger(){
		mergerContexts = new JavaUtilMapStorageBackend<CdrMergerContext>();
		persistMgr = new FileSysCdrPersistenceManager();
	}

}
