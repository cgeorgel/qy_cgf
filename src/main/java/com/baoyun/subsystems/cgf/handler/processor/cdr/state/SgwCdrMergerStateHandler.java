package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.SgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

public abstract class SgwCdrMergerStateHandler extends MergerStateHandler {

	private static Logger log = LoggerFactory.getLogger(SgwCdrMergerStateHandler.class);

	protected SgwCdrMergerContext context;

	protected SgwCdrMergerState theState;
	

	public SgwCdrMergerStateHandler(SgwCdrMergerContext context) {

		this.context = context;
	}

	/**
	 * @return the context
	 */
	@Override
	public SgwCdrMergerContext getContext() {
		// TODO Auto-generated method stub
		return context;
	}

	
	/**
	 * @return the theState
	 */
	public SgwCdrMergerState getTheState() {

		return theState;
	}
	
	/**
	 * <p>
	 * 当事件满足过滤条件, 则不对其进行处理, 状态机的状态不变.
	 * </p>
	 *
	 * @param event
	 * @return 若满足过滤条件, 返回true; 否则返回false.
	 */
	protected final boolean filter(CdrProcessingEvent event) {

		log.trace("filtering event: {}, current state: {}, context: {}.", new Object[] { event,
				getTheState(), getContext() });

		return doFilter(event);
	}

	protected boolean doFilter(CdrProcessingEvent event) {

		return false;
	}
	
	@Override
	public void handle(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		if (filter(event)) {
			log.trace("filtered event: {}, state machine's state does NOT change at all.", event);
			return;
		}

		log.trace("handling event: {}, current state: {}, context: {}.", new Object[] { event,
				getTheState(), getContext() });

		doHandle(event);
	}
	
	protected abstract void doHandle(CdrProcessingEvent event);

	@Override
	public void enterState(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		log.trace("enter state: {}, context: {}.", getTheState(), getContext());

		doEnterState(event);
	}
	
	protected void doEnterState(CdrProcessingEvent event) {

	}
	
	

	@Override
	public void exitState(CdrProcessingEvent event) {
		// TODO Auto-generated method stub
		log.trace("exit state: {}, context: {}.", getTheState(), getContext());

		doExitState(event);
	}
	
	protected void doExitState(CdrProcessingEvent event) {

	}

	public abstract SgwCdrMergerState getNextState(CdrProcessingEvent event);

}
