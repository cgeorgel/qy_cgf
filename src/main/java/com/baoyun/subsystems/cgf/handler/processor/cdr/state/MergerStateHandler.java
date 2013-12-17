package com.baoyun.subsystems.cgf.handler.processor.cdr.state;

import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrMergerContext;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

public abstract class MergerStateHandler {

	public abstract CdrMergerContext getContext();

	public abstract void handle(CdrProcessingEvent event);

	public abstract void enterState(CdrProcessingEvent event);

	public abstract void exitState(CdrProcessingEvent event);
}
