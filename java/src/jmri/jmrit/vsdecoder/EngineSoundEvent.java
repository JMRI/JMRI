package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;

import jmri.Throttle;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
 * @author Mark Underwood Copyright (C) 2011
 * @author Klaus Killinger Copyright (C) 2018
 */
public class EngineSoundEvent extends SoundEvent {

    EnginePane engine_pane;

    /*
     Trigger t; // used in setXml as a temporary holder for creating the
     // event listener class.

     ButtonTrigger bt; // used in setupButtonAction() as a temporary holder
     // for creating the button listeners.
     */

    public EngineSoundEvent() {
        this(null, null);
    }

    public EngineSoundEvent(String n) {
        this(n, n);
    }

    public EngineSoundEvent(String n, String bl) {
        super(n, bl);
        engine_pane = null;
    }

    @Override
    public boolean hasButton() {
        if ((buttontype == ButtonType.NONE) || (buttontype == ButtonType.ENGINE) || (button == null)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean hasEnginePane() {
        if ((buttontype == ButtonType.ENGINE) && (engine_pane != null)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public JComponent getButton() {
        log.debug("engine getButton() called.");
        return engine_pane;
    }

    @Override
    public EnginePane getEnginePane() {
        return engine_pane;
    }

    @Override
    public void setEnginePane(EnginePane e) {
        engine_pane = e;
    }

    @Override
    public void setButtonLabel(String bl) {
        // can't do this.  Yet.
    }

    @Override
    public String getButtonLabel() {
        // can't do this. Yet.
        //return(engine_pane.getText());
        return "Text";
    }

    @Override
    protected ButtonTrigger setupButtonAction(Element te) {
        /*
         MouseListener ml;
         bt = new ButtonTrigger(te.getAttributeValue("name"));
         button_trigger_list.put(bt.getName(), bt);
         log.debug("new ButtonTrigger " + bt.getName() + " type " + btype.toString());
         switch(btype) {
         case TOGGLE:
         this.getButton().addActionListener(bt);
         break;
         case MOMENTARY:
         default:
         this.getButton().addMouseListener(bt);
         // Just send the trigger a click.
         }
         return(bt);  // cast OK since we just instantiated it up above.
         */
        return null;  // cast OK since we just instantiated it up above.
    }

    public void guiAction(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("start")) {
            log.debug("GUI Start button changed. New value: {}", evt.getNewValue());
            if ((Boolean) evt.getNewValue()) {
                ((EngineSound) parent.getSound("ENGINE")).setEngineStarted(true);
                ((EngineSound) parent.getSound("ENGINE")).startEngine();
            } else {
                ((EngineSound) parent.getSound("ENGINE")).setEngineStarted(false);
                ((EngineSound) parent.getSound("ENGINE")).stopEngine();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            ((EngineSound) parent.getSound("ENGINE")).handleSpeedChange((Float) event.getNewValue(), engine_pane);
        } else if (event.getPropertyName().equals(Throttle.ISFORWARD)) {
            ((EngineSound) parent.getSound("ENGINE")).changeLocoDirection((Boolean) event.getNewValue() ? 1 : -1);
            log.debug("is forward: {}", event.getNewValue());
        } else if (event.getPropertyName().startsWith("F")) {
            String ev = event.getPropertyName();
            boolean val = (Boolean) event.getNewValue();
            for (Trigger t : trigger_list.values()) {
                log.debug("trigger name: {}, event: {}, target: {}", t.getName(), t.getEventName(), t.getTargetName());
                if (ev.equals(t.getEventName())) {
                    if (t.getName().equals("ENGINE_STARTSTOP")) {
                        getEnginePane().startButtonClick();
                    } else {
                        ((EngineSound) parent.getSound("ENGINE")).functionKey(ev, val, t.getName());
                        log.debug("event {} is {}", ev, val);
                    }
                }
            }
        }
        //engine_pane.setThrottle(EngineSound.calcEngineNotch((Float)event.getNewValue()));
    }

    @Override
    public Element getXml() {
        Element me = new Element("SoundEvent");
        me.setAttribute("name", name);
        me.setAttribute("label", me.getText());
        for (Trigger t : trigger_list.values()) {
            me.addContent(t.getXml());
        }
        return me;
    }

    @Override
    public void setXml(Element el) {
        this.setXml(el, null);
    }

    @Override
    public void setXml(Element el, VSDFile vf) {
        // Create the "button"  (should this be in constructor)
        log.debug("Creating DieselPane");
        engine_pane = new jmri.jmrit.vsdecoder.swing.DieselPane("Engine");

        // Handle common stuff
        super.setXml(el, vf);

        // Get the SoundEvent's button type and create it.
        engine_pane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                guiAction(evt);
            }
        });

        if (log.isDebugEnabled()) {
            for (ButtonTrigger bt : button_trigger_list.values()) {
                log.debug("Button Trigger: {}", bt.getName());
                log.debug("  Target: {}", bt.getTarget().getName());
                log.debug("  Action: {}", bt.getTargetAction());
            }
            for (Trigger bt : trigger_list.values()) {
                log.debug("Trigger: {}", bt.getName());
                log.debug("  Target: {}", bt.getTarget());
                log.debug("  Action: {}", bt.getTargetAction());
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(EngineSoundEvent.class);

}
