package com.baoyun.subsystems.cgf.handler.processor.cdr.state.impl;

import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrMerger;
import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.MergerStateHandler;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerState;
import com.baoyun.subsystems.cgf.handler.processor.cdr.state.PgwCdrMergerStateHandler;

/**
 * <p>
 * 特殊的StateHandler.
 * </p>
 *
 * <p>
 * 状态机的启动实际由{@link CdrMerger} -&gt; {@link CdrMergerContext#action(CdrProcessingEvent)} -&gt;
 * {@link MergerStateHandler#handle(CdrProcessingEvent)}方法为入口; 在此,
 * handle()的实现仅仅是将MergerContext的状态更改为 {@link PgwCdrMergerState#INITIAL}, 而INITIAL的StateHandler是
 * {@link InstantaneousStateHandler}, 可在 {@link #enterState(CdrProcessingEvent)}处正式启动状态机的执行.
 * </p>
 *
 * @author George
 *
 */
public class PgwCdrNullStateHandler extends PgwCdrMergerStateHandler {

	public PgwCdrNullStateHandler(PgwCdrMergerContext context) {

		super(context);
		theState = PgwCdrMergerState.NULL_STATE;
	}

	@Override
	public void doHandle(CdrProcessingEvent event) {

		getContext().changeState(getNextState(event), event);
	}

	/**
	 * Suppress the state entering log by super class.
	 */
	@Override
	public void enterState(CdrProcessingEvent event) {

	}

	/**
	 * Suppress the state entering log by super class.
	 */
	@Override
	public void exitState(CdrProcessingEvent event) {

	}

	@Override
	public PgwCdrMergerState getNextState(CdrProcessingEvent event) {

		return PgwCdrMergerState.INITIAL;
	}

}
