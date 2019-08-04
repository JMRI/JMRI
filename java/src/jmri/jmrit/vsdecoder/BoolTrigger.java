package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.beans.PropertyChangeEvent;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BoolTrigger extends Trigger {

    boolean match_value;

    public BoolTrigger(String name) {
        this(name, false);
    }

    public BoolTrigger(String name, boolean bv) {
        super(name);
        this.setTriggerType(Trigger.TriggerType.BOOLEAN);
        match_value = bv;
    }

    public void setMatchValue(boolean bv) {
        match_value = bv;
    }

    public boolean getMatchValue() {
        return (match_value);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Validate
        // If no target, or not a name match, or no trigger, or no action
        // then just return quickly.
        // Careful: Takes advantage of "lazy OR" behavior
        if (target == null) {
            //log.debug("Quit.  No target.");
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

        // Compare
        if (match_value == (Boolean) event.getNewValue()) {
            this.callback.takeAction();
        }
    }

    @Override
    public Element getXml() {
        Element me = new Element("trigger");

        log.debug("Bool Trigger getXml():");
        log.debug("  trigger_name = " + this.getName());
        log.debug("  event_name = " + this.event_name);
        log.debug("  target_name = " + target.getName());
        log.debug("  match = " + Boolean.valueOf(match_value).toString());
        log.debug("  action = " + this.getTriggerType().toString());

        me.setAttribute("name", this.getName());
        me.setAttribute("type", "BOOLEAN");
        me.addContent(new Element("event-name").addContent(event_name));
        me.addContent(new Element("target-name").addContent(target.getName()));
        me.addContent(new Element("match").addContent(Boolean.valueOf(match_value).toString()));
        me.addContent(new Element("action").addContent(this.getTriggerType().toString()));

        return (me);
    }

    @Override
    public void setXml(Element e) {
        // Get common stuff
        super.setXml(e);
        // Only do this if this is a BoolTrigger type Element
        if (e.getAttribute("type").getValue().equals("BOOLEAN")) {
            match_value = Boolean.parseBoolean(e.getChild("match").getValue());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(BoolTrigger.class);

}
