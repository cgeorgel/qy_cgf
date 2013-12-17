package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

/**
 * <p>
 * templates:
 * <ul>
 * <li>doEnterState(): 调用handle()以进行StateHandler的实际工作;<br />
 * 调用抽象方法getNextState()以判断下一状态;<br />
 * 调用{@link PgwCdrMergerContext#changeState(PgwCdrMergerState, CdrProcessingEvent)}以触发状态机的状态跳转.</li>
 * </ul>
 * </p>
 *
 * @author George
 *
 */
public abstract class InstantaneousStateHandler extends PgwCdrMergerStateHandler {

	public InstantaneousStateHandler(PgwCdrMergerContext context) {

		super(context);
	}

	@Override
	protected final void doEnterState(CdrProcessingEvent event) {

		handle(event);

		PgwCdrMergerState newState = getNextState(event);

		getContext().changeState(newState, event);
		
	}
}
