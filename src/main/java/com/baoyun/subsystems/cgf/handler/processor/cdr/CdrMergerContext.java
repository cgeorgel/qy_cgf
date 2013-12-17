package com.baoyun.subsystems.cgf.handler.processor.cdr;

import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;

/**
 * <p>
 * role: state context
 * </p>
 *
 * 
 *
 */
public abstract class CdrMergerContext {

	public CdrMergerContext() {

	}

	public abstract void action(CdrProcessingEvent event);

	public abstract String getUniqueId();
}
