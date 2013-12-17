package com.baoyun.subsystems.cgf.handler.processor.cdr.event;

public abstract class CdrProcessingEvent {

	protected Object arg;

	public CdrProcessingEvent() {

	}

	public CdrProcessingEvent(Object eventObject) {

		this.arg = eventObject;
	}

	public Object getArg() {

		return arg;
	}
}
