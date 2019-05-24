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
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jdom2.Element;

abstract public class Trigger implements PropertyChangeListener {

    static public enum TriggerType {

        BUTTON, BOOLEAN, STRING, NONE, NOTCH, INT, FLOAT, THROTTLE
    }

    static public enum TargetAction {

        PLAY, LOOP, STOP, FADEIN, FADEOUT, NOTCH, CHANGE, NOTHING
    }

    static public enum CompareType {

        EQ, GT, LT, GTE, LTE
    }

    static public enum CompareValueType {

        INT, FLOAT
    }

    String trigger_name; // Name for the trigger object
    String event_name;  // event to respond to
    String target_name; // target to act on

    VSDSound target;    // sound to work on
    private TargetAction target_action; // action to take
    private TriggerType trigger_type;
    TriggerListener callback;

    public Trigger(String name) {
        trigger_name = name;
        event_name = "";
        target = null;
        target_action = TargetAction.NOTHING;
        trigger_type = TriggerType.NONE;
    }

    @Override
    abstract public void propertyChange(PropertyChangeEvent event);

    // JavaBean set/get functions
    public void setName(String tn) {
        trigger_name = tn;
    }

    public String getName() {
        return (trigger_name);
    }

    public void setEventName(String en) {
        event_name = en;
    }

    public String getEventName() {
        return (event_name);
    }

    public void setTarget(VSDSound tgt) {
        target = tgt;
    }

    public VSDSound getTarget() {
        return (target);
    }

    public void setTargetName(String tn) {
        target_name = tn;
    }

    public String getTargetName() {
        return (target_name);
    }

    public void setTargetAction(Trigger.TargetAction ta) {
        target_action = ta;
    }

    public Trigger.TargetAction getTargetAction() {
        return (target_action);
    }

    public void setTriggerType(Trigger.TriggerType ta) {
        trigger_type = ta;
    }

    public Trigger.TriggerType getTriggerType() {
        return (trigger_type);
    }

    public void setCallback(TriggerListener cb) {
        callback = cb;
    }

    public TriggerListener getCallback() {
        return (callback);
    }

    public Element getXml() {
        Element me = new Element("Trigger");
        me.setAttribute("name", trigger_name);
        me.setAttribute("type", "empty");
        // do something, eventually...
        return (me);
    }

    public void setXml(Element e) {
        // Grab XML content that's common to all Triggers
        trigger_name = e.getAttributeValue("name");
        event_name = e.getChild("event-name").getValue();
        target_name = e.getChild("target-name").getValue();
        try {
            this.setTargetAction(Trigger.TargetAction.valueOf(e.getChild("action").getValue()));
        } catch (IllegalArgumentException iea) {
            this.setTargetAction(Trigger.TargetAction.NOTHING);
        } catch (NullPointerException npe) {
            this.setTargetAction(Trigger.TargetAction.NOTHING);
        }
    }

    //private static final Logger log = LoggerFactory.getLogger(Trigger.class);
}
