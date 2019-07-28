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

class IntTrigger extends Trigger {

    int notch;
    CompareType compare_type;

    public IntTrigger(String name) {
        this(name, 0, CompareType.EQ);
    }

    public IntTrigger(String name, int next, Trigger.CompareType ct) {
        super(name);
        this.setTriggerType(Trigger.TriggerType.INT);
        notch = next;
        compare_type = ct;
    }

    public void setMatchValue(int next) {
        notch = next;
    }

    public int getMatchValue() {
        return (notch);
    }

    public void setCompareType(IntTrigger.CompareType ct) {
        compare_type = ct;
    }

    public CompareType getCompareType() {
        return (compare_type);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        int next;
        boolean compare = false;

        // Validate
        // If no target, or not a name match, or no trigger, or no action
        // then just return quickly.
        // Careful: Takes advantage of "lazy OR" behavior
        if (target == null) {
            log.debug("Quit.  No target.");
            return;
        }
        if (event.getPropertyName().equals(this.getEventName()) != true) {
            log.debug("Quit. Event name mismatch event = " + event.getPropertyName() + " this = " + this.getEventName());
            return;
        }
        if (this.getTriggerType() == TriggerType.NONE) {
            log.debug("Quit.  TriggerType = NONE");
            return;
        }
        if (this.getTargetAction() == TargetAction.NOTHING) {
            log.debug("Quit.  TargetAction = NOTHING");
            return;
        }

        // Compare
        next = (Integer) event.getNewValue();
        switch (compare_type) {
            case GT:
                compare = (next > notch);
                break;
            case LT:
                compare = (next < notch);
                break;
            case GTE:
                compare = (next >= notch);
                break;
            case LTE:
                compare = (next <= notch);
                break;
            case EQ:
            default:
                compare = (next == notch);
                break;
        }

        if (compare) {
            this.callback.takeAction();
        }
    }

    @Override
    public void setXml(Element e) {
        // Grab common stuff.
        super.setXml(e);
        // Only do this if this is a BoolTrigger type Element
        if (e.getAttribute("type").getValue().equals("INT")) {
            notch = Integer.parseInt(e.getChild("match").getValue());
            compare_type = Trigger.CompareType.valueOf(e.getChild("compare-type").getValue().toUpperCase());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IntTrigger.class);

}
