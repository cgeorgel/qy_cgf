package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

/**
 * <p>
 * templates:
 * <ul>
 * <li>filter(): 记录日志, 调用doFilter();</li>
 * <li>handle(): 记录日志, 调用filter()以进行过滤处理, 最后调用doHandle().</li>
 * <li>enterState(): 记录日志, 调用doEnterState();</li>
 * <li>exitState(): 记录日志, 调用doExitState().</li>
 * </ul>
 * </p>
 *
 * <p> 
 * default hooks:
 * <ul>
 * <li>doFilter(): 空实现;</li>
 * <li>enterState(), exitState(): 空实现;</li>
 * <li>doHandle(): 空实现.</li>
 * </ul>
 * </p>
 *
 * TODO: 将{@link #enterState(CdrProcessingEvent)}, {@link #exitState(CdrProcessingEvent)}做成template.
 *
 * 
 *
 */
public abstract class PgwCdrMergerStateHandler extends MergerStateHandler {

	private static Logger log = LoggerFactory.getLogger(PgwCdrMergerStateHandler.class);

	protected PgwCdrMergerContext context;

	protected PgwCdrMergerState theState;

	public PgwCdrMergerStateHandler(PgwCdrMergerContext context) {

		this.context = context;
	}

	/**
	 * @return the context
	 */
	@Override
	public PgwCdrMergerContext getContext() {

		return context;
	}

	/**
	 * @return the theState
	 */
	public PgwCdrMergerState getTheState() {

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
	public final void handle(CdrProcessingEvent event) {

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

		log.trace("enter state: {}, context: {}.", getTheState(), getContext());

		doEnterState(event);
	}

	protected void doEnterState(CdrProcessingEvent event) {

	}

	@Override
	public void exitState(CdrProcessingEvent event) {

		log.trace("exit state: {}, context: {}.", getTheState(), getContext());

		doExitState(event);
	}

	protected void doExitState(CdrProcessingEvent event) {

	}

	public abstract PgwCdrMergerState getNextState(CdrProcessingEvent event);
}
