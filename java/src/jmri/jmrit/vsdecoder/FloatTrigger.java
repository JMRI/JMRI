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
 * @author   Mark Underwood Copyright (C) 2011
 * 
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FloatTrigger extends Trigger implements PropertyChangeListener {

    Float match_value;
    CompareType compare_type;

    public FloatTrigger(String name, Float next, Trigger.CompareType ct) {
        super(name);
        this.setTriggerType(Trigger.TriggerType.FLOAT);
        match_value = next;
        compare_type = ct;
    }

    public void setMatchValue(Float next) {
        match_value = next;
    }

    public Float getMatchValue() {
        return (match_value);
    }

    public void setCompareType(CompareType ct) {
        compare_type = ct;
    }

    public CompareType getCompareType() {
        return (compare_type);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        Float next;
        boolean compare = false;
        int compare_val;

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
        next = (Float) event.getNewValue(); // HACK!  Needs to be flexible.
        compare_val = next.compareTo(match_value);
        switch (compare_type) {
            case GT:
                compare = (compare_val > 0);
                break;
            case LT:
                compare = (compare_val < 0);
                break;
            case GTE:
                compare = (compare_val >= 0);
                break;
            case LTE:
                compare = (compare_val <= 0);
                break;
            case EQ:
            default:
                compare = (compare_val == 0);
                break;
        }

        log.debug("compareTrigger match_value = " + match_value + " next = " + next + " compare_val = " + compare_val + " compare = " + compare);

        if (compare) {
            log.debug("compareTrigger taking action");
            this.callback.takeAction();
        }
    }

    @Override
    public Element getXml() {
        Element me = new Element("trigger");
        me.setAttribute("name", this.getName());
        me.setAttribute("type", "FLOAT");
        log.warn("CompareTrigger.getXml() not implemented");
        return (me);
    }

    @Override
    public void setXml(Element e) {
        log.debug("FloatTrigger.setXml()");

        //Get common stuff
        super.setXml(e);

        if (e.getAttributeValue("type").equals("FLOAT")) {
            match_value = Float.parseFloat(e.getChild("match").getValue() + "f");

            compare_type = Trigger.CompareType.valueOf(e.getChild("compare-type").getValue().toUpperCase());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(FloatTrigger.class);

}
