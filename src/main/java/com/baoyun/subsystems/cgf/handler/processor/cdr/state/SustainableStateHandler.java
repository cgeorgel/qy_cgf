package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

/**
 * <p>
 * templates:
 * <ul>
 * <li>doHandle(): 调用doHandleLogic(); 调用getNextState()以判断下一状态, 然后再决定是否调用
 * {@link PgwCdrMergerContext#changeState(PgwCdrMergerState, CdrProcessingEvent)}改变状态机的状态.</li>
 * </ul>
 * </p>
 *
 * <p>
 * abstract methods:
 * <ul>
 * <li>doHandleLogic(): 进行StateHandler的实际操作实现;</li>
 * <li>getNextState(): 判断下一个状态将是什么.</li>
 * </ul>
 * </p>
 *
 *
 *
 */
public abstract class SustainableStateHandler extends PgwCdrMergerStateHandler {

	public SustainableStateHandler(PgwCdrMergerContext context) {

		super(context);
	}

	@Override
	protected final void doHandle(CdrProcessingEvent event) {

		doHandleLogic(event);

		PgwCdrMergerState nextState = getNextState(event);

		if (!nextState.equals(getContext().getCurrentState())) {
			getContext().changeState(nextState, event);
		}
	}

	protected abstract void doHandleLogic(CdrProcessingEvent event);

	@Override
	public abstract PgwCdrMergerState getNextState(CdrProcessingEvent event);
}
