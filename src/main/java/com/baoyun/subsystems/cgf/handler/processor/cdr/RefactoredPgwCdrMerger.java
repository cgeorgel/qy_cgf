package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.asn1.InputPgwCdrObject;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

import net.rubyeye.xmemcached.exception.MemcachedException;
import ch.panter.li.bi.asn.AsnValue;

public class RefactoredPgwCdrMerger extends PgwCdrMerger {

	private static Logger log = LoggerFactory.getLogger(RefactoredPgwCdrMerger.class);

	public RefactoredPgwCdrMerger() {

	}

	@Override
	public boolean processReceivedCdr(CdrProcessingEvent event) throws TimeoutException,
			InterruptedException, MemcachedException, Exception {

		boolean newCdrAdded = false;

		InputPgwCdrObject inputPgwCdr;

		Object inputCdr = event.getArg();
		if (inputCdr instanceof InputPgwCdrObject) {
			inputPgwCdr = (InputPgwCdrObject) inputCdr;
		} else {
			 inputPgwCdr = genInputPgwCdrObject((AsnValue) inputCdr);
		}

		log.trace("start processing input P-GW CDR: {}.", inputPgwCdr);
//		MiscUtils.periodicallyDumpThread(10, 1); // temp debug: dump thread later.

		PgwCdrCategoryKey key = inputPgwCdr.genPgwCdrCategoryKey();

		PgwCdrMergerContext mergerCtxt;
		if (mergerContexts.contains(key.toString())) {

			mergerCtxt = (PgwCdrMergerContext) mergerContexts.get(key.toString());
			mergerCtxt.action(event);
		} else {
			// 某个key的P-GW CDR的首次接收: 创建相应的状态机context(刚创建完其状态为NULL_STATE), 并触发状态机的启动.
			mergerCtxt = (PgwCdrMergerContext) createCdrMergerContext(inputPgwCdr);
			mergerContexts.put(key.toString(), mergerCtxt);
			mergerCtxt.action(event);
		}

		return newCdrAdded;
	}

	public InputPgwCdrObject genInputPgwCdrObject(AsnValue inputPgwCdr)  {

		return new InputPgwCdrObject(inputPgwCdr);
	}

	protected CdrMergerContext createCdrMergerContext(InputPgwCdrObject inputPgwCdr) {

		PgwCdrCategoryKey key = inputPgwCdr.genPgwCdrCategoryKey();

		PgwCdrMergerContext context = new PgwCdrMergerContext(this, key);

		return context;
	}

	@Override
	public void clearCdrMergerContext(PgwCdrCategoryKey key) {

		mergerContexts.delete(key.toString());
	}
}
