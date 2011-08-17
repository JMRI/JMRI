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
 * @version			$Revision$
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JComponent;
import javax.swing.AbstractButton;
import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.Collection;

public class EngineSoundEvent extends SoundEvent implements PropertyChangeListener {

    EnginePane engine_pane;

    Trigger t; // used in setXml as a temporary holder for creating the
               // event listener class.
    ButtonTrigger bt; // used in setupButtonAction() as a temporary holder
                      // for creating the button listeners.

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

    public boolean hasButton() {
	if ((buttontype == ButtonType.NONE) || (buttontype == ButtonType.ENGINE) || (button == null))
	    return(false);
	else
	    return(true);
    }

    public boolean hasEnginePane() {
	if ((buttontype == ButtonType.ENGINE) && (engine_pane != null))
	    return(true);
	else
	    return(false);
    }

    public JComponent getButton() {
	log.debug("engine getButton() called.");
	return(engine_pane);
    }

    public EnginePane getEnginePane() {
	return(engine_pane);
    }

    public void setEnginePane(EnginePane e) {
	engine_pane = e;
    }

    public void setButtonLabel(String bl) {
	// can't do this.  Yet.
    }

    public String getButtonLabel() {
	// can't do this. Yet.
	//return(engine_pane.getText());
	return("Text");
    }

    private void mouseDown() {
    }

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
	return(null);  // cast OK since we just instantiated it up above.
    }

    public void guiAction(PropertyChangeEvent evt) {
	if (evt.getPropertyName().equals("throttle")) {
	    log.debug("GUI throttle changed.  New value = " + (Integer)evt.getNewValue());
	} else if (evt.getPropertyName().equals("start")) {
	    log.debug("GUI Start button changed. New value = " + (Boolean)evt.getNewValue());
	    if ((Boolean)evt.getNewValue() == true) {
		((EngineSound)parent.getSound("ENGINE")).startEngine();
	    } else {
		((EngineSound)parent.getSound("ENGINE")).stopEngine();
	    }
	}
    }

    public void propertyChange(PropertyChangeEvent event) {
	int th;
	super.propertyChange(event);
	if (event.getPropertyName().equals("SpeedSetting"))
	    engine_pane.setThrottle(VSDecoder.calcEngineNotch((Float)event.getNewValue()));
    }



    public Element getXml() {
	Element me = new Element("SoundEvent");
	me.setAttribute("name", name);
	me.setAttribute("label", me.getText());
	for (Trigger t : trigger_list.values()) {
	    me.addContent(t.getXml());
	}

	return(me);
    }

    public void setXml(Element el) {
	this.setXml(el, null);
    }

    @Override
    public void setXml(Element el, VSDFile vf) {
	Element te;
	String bav;

	// Create the "button"  (should this be in constructor)
	log.debug("Creating DieselPane");
	engine_pane = new DieselPane("Engine");

	// Handle common stuff
	super.setXml(el, vf);

	// Get the SoundEvent's button type and create it.
	engine_pane.addPropertyChangeListener(new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
		    guiAction(evt);
		}
	    });

	for (ButtonTrigger bt : button_trigger_list.values()) {
	    log.debug("Button Trigger: " + bt.getName());
	    log.debug("  Target: " + bt.getTarget().getName());
	    log.debug("  Action: " + bt.getTargetAction().toString());
	}

	for (Trigger bt :trigger_list.values()) {
	    log.debug("Trigger: " + bt.getName());
	    log.debug("  Target: " + bt.getTarget());
	    log.debug("  Action: " + bt.getTargetAction().toString());
	}
    }  // end setXml()

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SoundEvent.class.getName());
    
}