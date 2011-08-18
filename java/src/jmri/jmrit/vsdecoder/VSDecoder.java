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

import javax.swing.Timer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.audio.*;
import jmri.AudioException;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Content;

class VSDecoder {

    private static final boolean bootstrap = false;
    private BoolTrigger t;  // used only for bootstrap

    private String vsd_path;
    private String vsd_filename;
					     

    String profile_name;           // Name used in the "profiles" combo box to select this decoder.
    DccLocoAddress address;        // Currently assigned loco address
    boolean initialized = false;   // Flag indicating whether this decoder has been initialized
    boolean enabled = false;
    private boolean is_default = false;

    HashMap<String, VSDSound> sound_list;

    // Trigger objects
    FloatTrigger engine_stop_trigger;
    NotchTrigger engine_notch_trigger;

    HashMap<String, Trigger> trigger_list;

    HashMap<String, SoundEvent> event_list;
    
    // Engine Control
    // engine_is_running indicates whether the engine is off or idling/running
    // engine_notch indicates the speed notch.
    // engine_notch = 0 && engine_is_running : engine at idle
    // engien_notch > 0 && engine_is_running : engine running at speed.
    boolean engine_is_running = false;
    int engine_notch = 0;

    //DccThrottle throttle;

    // Temp stuff for debugging VSDecoderManager.
    String bell_fname = "F1";
    String horn_fname = "F2";


    static final public int calcEngineNotch(final float throttle) {
	// This will convert to a value 0-8.
	int notch = (int) Math.rint(throttle * 8);
	if (notch < 0) { notch = 0; }
	log.warn("Throttle: " + throttle + " Notch: " + notch);
	return(notch+1);

    }

    static final public int calcEngineNotch(final double throttle) {
	// This will convert from a % to a value 0-8.
	int notch = (int) Math.rint(throttle * 8);
	if (notch < 0) { notch = 0; }
	//log.warn("Throttle: " + throttle + " Notch: " + notch);
	return(notch+1);

    }

    public VSDecoder(String name)  throws AudioException {
//	jmri.InstanceManager.audioManagerInstance().provideAudio("IAS");

	profile_name = name;

	sound_list = new HashMap<String, VSDSound>();
	trigger_list = new HashMap<String, Trigger>();
	event_list = new HashMap<String, SoundEvent>();

	// Force re-initialization
	initialized = _init();
    }

    public VSDecoder(String name, int address, boolean isLong) throws AudioException {
        this(name);
        setAddress(address, isLong);
    }

    private void buildElements() {
	// does nothing now.
    }

    private boolean _init() {

	buildElements();

	return(true);
    }

    public void setVSDFilePath(String p) {
	vsd_path = p;
    }

    public String getVSDFilePath() {
	return(vsd_path);
    }

    public void setVSDFileName(String p) {
	vsd_filename = p;
    }

    public String getVSDFileName() {
	return(vsd_filename);
    }

    public void throttlePropertyChange(PropertyChangeEvent event) {
	//WARNING: FRAGILE CODE
	// This will break if the return type of the event.getOld/NewValue() changes.
	
	String eventName = event.getPropertyName();
	Object oldValue = event.getOldValue();
	Object newValue = event.getNewValue();

	// Skip this if disabled
	if (!enabled)
	    return;

	log.warn("VSDecoderPane throttle property change: " + eventName);

	if (oldValue != null)
	    log.warn("Old: " + oldValue.toString());
	if (newValue != null)
	    log.warn("New: " + newValue.toString());

	// Iterate through the list of sound events, forwarding the propertyChange event.
	for (SoundEvent t : event_list.values()) {
	    t.propertyChange(event);
	}

	// Iterate through the list of triggers, forwarding the propertyChange event.
	for (Trigger t : trigger_list.values()) {
	    t.propertyChange(event);
	}
    }

    public void releaseAddress(int number, boolean isLong) {
	// remove the listener, if we can...
    }

    public void setAddress(int number, boolean isLong) {
	this.setAddress(new DccLocoAddress(number, isLong));
    }

    public void setAddress(DccLocoAddress a) {
	address = a;
	jmri.InstanceManager.throttleManagerInstance().attachListener(address, new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
		    throttlePropertyChange(event);
		}
	    });
	log.debug("VSDecoder: Address set to " + address.toString());
    }

    public DccLocoAddress getAddress() {
	return(address);
    }

    public VSDSound getSound(String name) {
	return(sound_list.get(name));
    }

    public void toggleBell() {
	VSDSound snd = sound_list.get("BELL");
        if(snd.isPlaying())
            snd.stop();
        else
            snd.loop();
    }
    
    public void toggleHorn() {
	VSDSound snd = sound_list.get("HORN");
        if(snd.isPlaying())
            snd.stop();
        else
            snd.loop();
    }

    public void playHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.loop();
    }

    public void shortHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.play();
    }

    public void stopHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.stop();
    }

    // Java Bean set/get Functions

    public void setProfileName(String pn) {
	profile_name = pn;
    }

    public String getProfileName() {
	return(profile_name);
    }
	
    public void enable() {
	enabled = true;
    }

    public void disable() {
	enabled = false;
    }

    public Collection getEventList() {
	return(event_list.values());
    }
    
    public boolean isDefault() {
	return(is_default);
    }

    public void setDefault(boolean d) {
	is_default = d;
    }

    public Element getXml() {
	Element me = new Element("vsdecoder");
	ArrayList<Element> le = new ArrayList<Element>();

	me.setAttribute("name", this.profile_name);

	// If this decoder is marked as default, add the default Element.
	if (is_default)
	    me.addContent(new Element("default"));
	
	for (SoundEvent se : event_list.values()) {
	    le.add(se.getXml());
	}

	for (VSDSound vs : sound_list.values()) {
	    le.add(vs.getXml());
	}

	for (Trigger t : trigger_list.values()) {
	    le.add(t.getXml());
	}

	
	me.addContent(le);

	// Need to add whatever else here.

	return(me);
    }

    public void setXml(Element e) {
	this.setXml(e, null);
    }

    public void setXml(Element e, VSDFile vf) {
	Iterator itr;
	Element el;
	SoundEvent se;
	
	// Set filename and path
	if (vf != null) {
	    log.debug("VSD File Name = " + vf.getName());
	    // need to choose one.
	    this.setVSDFilePath(vf.getName());
	    this.setVSDFileName(vf.getName());
	}

	// Set this decoder's name.
	this.setProfileName(e.getAttributeValue("name"));
	log.debug("Decoder Name = " + e.getAttributeValue("name"));
	if(vf != null) {
	}

	// Read and create all of its components.

	// Check for default element.
	if (e.getChild("default") != null) {
	    log.debug("" + getProfileName() + "is default.");
	    is_default = true;
	}
	else {
	    is_default = false;
	}

	// Log and print all of the child elements.
	itr = (e.getChildren()).iterator();
	while(itr.hasNext()) {
	    // Pull each element from the XML file.
	    el = (Element)itr.next();
	    log.debug("Element: " + el.toString());
	    if (el.getAttribute("name") != null) {
		log.debug("  Name: " + el.getAttributeValue("name"));
		log.debug("   type: " + el.getAttributeValue("type"));
	    }
	}


	// First, the sounds.
	itr = (e.getChildren("sound")).iterator();
	while(itr.hasNext()) {
	    el = (Element)itr.next();
	    if (el.getAttributeValue("type") == null) {
		// Empty sound.  Skip.
		log.debug("Skipping empty Sound.");
		continue;
	    } else if (el.getAttributeValue("type").equals("configurable")) {
		// Handle configurable sounds.
		ConfigurableSound cs = new ConfigurableSound(el.getAttributeValue("name"));
		cs.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"),cs);
	    } else if (el.getAttributeValue("type").equals("diesel")) {
		// Handle a Diesel Engine sound
		EngineSound es = new EngineSound(el.getAttributeValue("name"));
		es.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"), es);
	    } else {
		//TODO: Some type other than configurable sound.  Handle appropriately
	    }
	}

	// Next, grab all of the SoundEvents
	// Have to do the sounds first because the SoundEvent's setXml() will
	// expect to be able to look it up.
	itr = (e.getChildren("sound-event")).iterator();
	while (itr.hasNext()) {
	    el = (Element)itr.next();
	    switch(SoundEvent.ButtonType.valueOf(el.getAttributeValue("buttontype").toUpperCase())) {
	    case MOMENTARY:
		se = new MomentarySoundEvent(el.getAttributeValue("name"));
		break;
	    case TOGGLE:
		se = new ToggleSoundEvent(el.getAttributeValue("name"));
		break;
	    case ENGINE:
		se = new EngineSoundEvent(el.getAttributeValue("name"));
		break;
	    case NONE:
	    default:
		se = new SoundEvent(el.getAttributeValue("name"));
	    }
	    se.setParent(this);
	    se.setXml(el, vf);
	    event_list.put(se.getName(), se);
	}

	/*
	// Now, grab any loose triggers
	// This may become obsolete...
	itr = (e.getChildren("trigger")).iterator();
	while(itr.hasNext() && false) {
	    el = (Element)itr.next();
	    Trigger t = null;
	    Trigger.TriggerType tt = Trigger.TriggerType.valueOf(el.getAttributeValue("type"));
	    switch(tt) {
	    case BOOLEAN:
		t = new BoolTrigger(el.getAttributeValue("name"));
		break;
	    case FLOAT:
		t = new FloatTrigger(el.getAttributeValue("name"), 0.0f, Trigger.CompareType.EQ); break;
	    case NOTCH:
		t = new NotchTrigger(el.getAttributeValue("name"));
		break;
	    case INT:
		t = new IntTrigger(el.getAttributeValue("name"));
		break;
	    case STRING:
		//t = new StringTrigger(el.getAttributeValue("name"));
		log.warn("Don't have StringTriggers yet...");
		t = null;
		break;
	    case NONE:
	    default:
		break;
	    }
	    if (t != null) {
		t.setXml(el);
		trigger_list.put(el.getAttributeValue("name"), t);
	    }
	}
	*/
	// Handle other types of children similarly here.
	
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoder.class.getName());

}
