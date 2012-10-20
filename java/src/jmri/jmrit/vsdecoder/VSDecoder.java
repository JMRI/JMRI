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
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import jmri.util.PhysicalLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.locations.Location;

import org.jdom.Element;

public class VSDecoder implements PropertyChangeListener {

    private String my_id;          // Unique ID for this VSDecoder
    private String vsd_path;       // Path to VSD file used
    private String profile_name;   // Name used in the "profiles" combo box
                                   // to select this decoder.
    LocoAddress address;        // Currently assigned loco address -- should be LocoAddress...
    boolean initialized = false;   // This decoder has been initialized
    boolean enabled = false;       // This decoder is enabled
    private boolean is_default = false;  // This decoder is the default for its file

    private float master_volume;

    private PhysicalLocation my_position;

    HashMap<String, VSDSound> sound_list;   // list of sounds
    HashMap<String, Trigger> trigger_list;  // list of triggers
    HashMap<String, SoundEvent> event_list; // list of events
    
    public VSDecoder(String id, String name) {

	profile_name = name;
	my_id = id;
	
	sound_list = new HashMap<String, VSDSound>();
	trigger_list = new HashMap<String, Trigger>();
	event_list = new HashMap<String, SoundEvent>();
	    
	// Force re-initialization
	initialized = _init();
    }


    public VSDecoder(String id, String name, String path) {
	this(id, name);

	vsd_path = path;

	try {
	    VSDFile vsdfile = new VSDFile(path);
	    if (vsdfile.isInitialized()) {
		log.debug("Constructor: vsdfile init OK, loading XML...");
		this.setXml(vsdfile, name);
	    } else {
		log.debug("Constructor: vsdfile init FAILED.");
		initialized = false;
	    }
	} catch (java.util.zip.ZipException e) {
	    log.error("ZipException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	} catch (java.io.IOException ioe) {
	    log.error("IOException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	}
    }

    private boolean _init() {
	// Do nothing for now
	return(true);
    }

    public String getID() { return(my_id); }

    public boolean isInitialized() { return(initialized); }

    public void setVSDFilePath(String p) {
	vsd_path = p;
    }

    public String getVSDFilePath() {
	return(vsd_path);
    }

    public void windowChange(java.awt.event.WindowEvent e) {
	log.debug("decoder.windowChange() - " + e.toString());
	log.debug("param string = " + e.paramString());
	//if (e.paramString().equals("WINDOW_CLOSING")) {
	    // Shut down the sounds.
	    this.shutdown();
	    
	//}
    }

    public void shutdown() {
	log.debug("Shutting down sounds...");
	for (VSDSound vs : sound_list.values()) {
	    log.debug("Stopping sound: " + vs.getName());
	    vs.shutdown();
	}
    }

    public void throttlePropertyChange(PropertyChangeEvent event) {
	//WARNING: FRAGILE CODE
	// This will break if the return type of the event.getOld/NewValue() changes.
	
	String eventName = event.getPropertyName();
	Object oldValue = event.getOldValue();
	Object newValue = event.getNewValue();

	// Skip this if disabled
	if (!enabled) {
	    log.debug("VSDecoder disabled. Take no action.");
	    return;
	}

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

    // DCC-specific and unused. Deprecate this.
    public void releaseAddress(int number, boolean isLong) {
	// remove the listener, if we can...
    }

    // DCC-specific.  Deprecate this.
    public void setAddress(int number, boolean isLong) {
	this.setAddress(new DccLocoAddress(number, isLong));
    }
    
    public void setAddress(LocoAddress l) {
	// Hack for ThrottleManager Dcc dependency
	address = l;
	DccLocoAddress dl = new DccLocoAddress(l.getNumber(), l.getProtocol());
	jmri.InstanceManager.throttleManagerInstance().attachListener(dl, new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
		    log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
		    throttlePropertyChange(event);
		}
	    });
	log.debug("VSDecoder: Address set to " + address.toString());
    }
    
    public LocoAddress getAddress() {
	return(address);
    }
    /*
    public DccLocoAddress getDccAddress() {
	return(new DccLocoAddress(address.getNumber(), address.getProtocol()));
    }
    */
    public float getMasterVolume() {
	return(master_volume);
    }

    public void setMasterVolume(float vol) {
	log.debug("VSD: float volume = " + vol);
	master_volume = vol;
	for (VSDSound vs : sound_list.values()) {
	    vs.setVolume(master_volume);
	}
    }

    public boolean isMuted() {
	return(false);
    }

    public void mute(boolean m) {
	for (VSDSound vs : sound_list.values()) {
	    vs.mute(m);
	}
    }

    public void setPosition(PhysicalLocation p) {
	my_position = p;
	log.debug("Set Position: " + my_position.toString());
	for (VSDSound s : sound_list.values()) {
	    s.setPosition(p);
	}
    }

    public PhysicalLocation getPosition() {
	return(my_position);
    }

    public void propertyChange(PropertyChangeEvent evt) {
	// Respond to events from the GUI.
	String property = evt.getPropertyName();
	if (property.equals("AddressChange")) {
	    this.setAddress((LocoAddress)evt.getNewValue());
	    this.enable();
	} else if (property.equals("Mute")) {
	    log.debug("VSD: Mute change. value = " + evt.getNewValue());
	    Boolean b = (Boolean)evt.getNewValue();
	    this.mute(b.booleanValue());
	} else if (property.equals("VolumeChange")) {
	    log.debug("VSD: Volume change. value = " + evt.getNewValue());
	    // Slider gives integer 0-100.  Need to change that to a float 0.0-1.0
	    this.setMasterVolume((1.0f * (Integer)evt.getNewValue())/100.0f);
	} else if (property.equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY)) {
	    PhysicalLocation p = getTrainPosition((Train)evt.getSource());
	    if (p != null)
		this.setPosition(getTrainPosition((Train)evt.getSource()));
	    else {
		log.debug("Train has null position");
		this.setPosition(new PhysicalLocation());
	    }
	} else if (property.equals(Train.STATUS_CHANGED_PROPERTY))  {
	    String status = (String)evt.getNewValue();
	    log.debug("Train status changed: " + status);
	    log.debug("New Location: " + getTrainPosition((Train)evt.getSource()));
	    if ((status.startsWith(Train.BUILT)) || (status.startsWith(Train.PARTIALBUILT))){ 
		log.debug("Train built. status = " + status);
		PhysicalLocation p = getTrainPosition((Train)evt.getSource());
		if (p != null)
		    this.setPosition(getTrainPosition((Train)evt.getSource()));
		else {
		    log.debug("Train has null position");
		    this.setPosition(new PhysicalLocation());
		}
	    }
	}
    }

    // Take a train and get its physical position
    // Returns null if anything is undefined.
    protected PhysicalLocation getTrainPosition(Train t) {
	if (t == null) {
	    log.debug("Train is null.");
	    return(null);
	}
	RouteLocation rloc = t.getCurrentLocation();
	if (rloc == null) {
	    log.debug("RouteLocation is null.");
	    return(null);
	}
	Location loc = rloc.getLocation();
	if (loc == null) {
	    log.debug("Location is null.");
	    return(null);
	}
	return(loc.getPhysicalLocation());
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

    public Collection<SoundEvent> getEventList() {
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

    /*
    @Deprecated
    public void setXml(Element e) {
	this.setXml(e, null);
    }

    @Deprecated
    public void setXml(Element e, VSDFile vf) {
	this.setXml(vf);
    }

    @Deprecated
    public void setXml(VSDFile vf) { }
    */

    @SuppressWarnings({"unchecked", "cast"})
    public void setXml(VSDFile vf, String pn) {
	Iterator<Element> itr;
	Element e = null;
	Element el = null;
	SoundEvent se;
        
        if (vf == null) {
            log.debug("Null VSD File Name");
            return;
        }
	
        log.debug("VSD File Name = " + vf.getName());
	// need to choose one.
	this.setVSDFilePath(vf.getName());

	// Find the <profile/> element that matches the name pn
	//List<Element> profiles = vf.getRoot().getChildren("profile");
	//java.util.Iterator i = profiles.iterator();
	java.util.Iterator<Element> i = vf.getRoot().getChildren("profile").iterator();
	while (i.hasNext()) {
	    e = i.next();
	    if (e.getAttributeValue("name").equals(pn))
		break;
	}
	// E is now the first <profile/> in vsdfile that matches pn.

        if (e == null) {
	    // No matching profile name found.
            return;
        }
        
	// Set this decoder's name.
	this.setProfileName(e.getAttributeValue("name"));
	log.debug("Decoder Name = " + e.getAttributeValue("name"));


	// Read and create all of its components.

	// Check for default element.
	if (e.getChild("default") != null) {
	    log.debug("" + getProfileName() + "is default.");
	    is_default = true;
	}
	else {
	    is_default = false;
	}

	// +++ DEBUG
	// Log and print all of the child elements.
	itr = (e.getChildren()).iterator();
	while(itr.hasNext()) {
	    // Pull each element from the XML file.
	    el = itr.next();
	    log.debug("Element: " + el.toString());
	    if (el.getAttribute("name") != null) {
		log.debug("  Name: " + el.getAttributeValue("name"));
		log.debug("   type: " + el.getAttributeValue("type"));
	    }
	}
	// --- DEBUG


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
		DieselSound es = new DieselSound(el.getAttributeValue("name"));
		es.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"), es);
	    } else if (el.getAttributeValue("type").equals("steam")) {
		// Handle a Diesel Engine sound
		SteamSound es = new SteamSound(el.getAttributeValue("name"));
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

	// Handle other types of children similarly here.
	
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoder.class.getName());

}
