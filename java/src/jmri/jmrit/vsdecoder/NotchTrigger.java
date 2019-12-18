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

class NotchTrigger extends Trigger {

    int current_notch, prev_notch;

    public NotchTrigger(String name) {
        this(name, 0, 0);
    }

    public NotchTrigger(String name, int prev, int cur) {
        super(name);
        this.setTriggerType(Trigger.TriggerType.NOTCH);
        prev_notch = prev;
        current_notch = cur;
    }

    public void setNotch(int next) {
        current_notch = next;
    }

    public int getNotch() {
        return (current_notch);
    }

    @Override
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

        // Compare
        prev_notch = current_notch;
        current_notch = EngineSound.calcEngineNotch((Float) event.getNewValue());

        log.debug("Notch Trigger prev_notch = " + prev_notch + " current_notch = " + current_notch);
        this.callback.takeAction(current_notch);
        /*
         if ((prev == prev_notch) && (next == next_notch)) {
         this.callback.takeAction();
         }
         */
    }

    @Override
    public Element getXml() {
        Element me = new Element("Trigger");
        me.setAttribute("name", this.getName());
        me.setAttribute("type", "NOTCH");
        log.warn("CompareTrigger.getXml() not implemented");
        return (me);
    }

    @Override
    public void setXml(Element e) {
        //Get common stuff
        super.setXml(e);
    }

    private static final Logger log = LoggerFactory.getLogger(NotchTrigger.class);

}
