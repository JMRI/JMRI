package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision: 18245 $
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// XML stuff
import org.jdom.Element;


class ThrottleTrigger extends Trigger implements PropertyChangeListener {

    int current_notch, prev_notch;

    public ThrottleTrigger(String name) {
	super(name);
	this.setTriggerType(Trigger.TriggerType.THROTTLE);
    }

    public void propertyChange(PropertyChangeEvent event) {

	// Validate
	// If no target, or not a name match, or no trigger, or no action
	// then just return quickly.
	// Careful: Takes advantage of "lazy OR" behavior
	if (target == null) {
	    log.debug("Quit.  No target.");
	    return;
	}
	if (event.getPropertyName().equals(this.getEventName()) != true) {
	    //log.debug("Quit. Event name mismatch event = " + event.getPropertyName() + " this = " + this.getEventName());
	    return;
	}
	if (this.getTriggerType() == TriggerType.NONE) {
	    //log.debug("Quit.  TriggerType = NONE");
	    return;
	}
	if (this.getTargetAction() == TargetAction.NOTHING) {
	    //log.debug("Quit.  TargetAction = NOTHING");
	    return;
	}

	
	log.debug("Throttle Trigger old value = " + event.getOldValue() + " new value = " + event.getNewValue());
	this.callback.takeAction((Float)event.getNewValue());
    }

    @Override
    public Element getXml() {
	Element me = new Element("Trigger");
	me.setAttribute("name", this.getName());
	me.setAttribute("type", "THROTTLE");
	log.warn("CompareTrigger.getXml() not implemented");
	return(me);
    }

    @Override
    public void setXml(Element e) {
	//Get common stuff
	super.setXml(e);
	// Only do this if type Notch
	if (e.getAttributeValue("type").equals("THROTTLE")) {
	    // Nothing unique to do...
	}
    }

    private static final Logger log = LoggerFactory.getLogger(ThrottleTrigger.class.getName());


}
