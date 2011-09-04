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

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import jmri.util.JmriJFrame;
import javax.swing.JFrame;

import jmri.jmrit.XmlFile;
import java.io.File;
import java.util.ResourceBundle;
import java.util.List;
import org.jdom.Element;

// VSDecoderFactory
//
// Builds VSDecoders as needed.  Handles loading from XML if needed.

class VSDecoderManager {

    public static enum EventType { NONE, DECODER_LIST_CHANGE }  // propertyChangeEvents fired by the Manager.

    private static final ResourceBundle rb = VSDecoderBundle.bundle();

    HashMap<String, VSDecoder> decodertable; // list of active decoders
    HashMap<String, String> profiletable;    // list of loaded profiles key = profile name, value = path

    // List of registered event listeners
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    //private static VSDecoderManager instance = null;   // sole instance of this class
    private static VSDecoderManagerThread thread = null; // thread for running the manager

    private VSDecoderPreferences vsdecoderPrefs; // local pointer to the preferences object
    private JmriJFrame vsdecoderPreferencesFrame; // Frame for holding the preferences GUI  (do we need this?)

    private VSDecoder default_decoder = null;  // shortcut pointer to the default decoder (do we need this?)

    private static int vsdecoderID = 0;

    // constructor - for kicking off by the VSDecoderManagerThread...
    // WARNING: Should only be called from static instance()
    public VSDecoderManager() {
	// Setup the decoder table
	decodertable = new HashMap<String, VSDecoder>();
	profiletable = new HashMap<String, String>();  // key = profile name, value = path
	// Get preferences
	String dirname = XmlFile.prefsDir()+ "vsdecoder" +File.separator;
	XmlFile.ensurePrefsPresent(dirname);
	vsdecoderPrefs = new VSDecoderPreferences(dirname+ rb.getString("VSDPreferencesFileName"));
    }

    public static VSDecoderManager instance() {
	if (thread == null) {
	    thread = VSDecoderManagerThread.instance(true);
	}
	return(VSDecoderManagerThread.manager());
    }

    public VSDecoderPreferences getVSDecoderPreferences() {
	return(vsdecoderPrefs);
    }

    private void buildVSDecoderPreferencesFrame() {
	vsdecoderPreferencesFrame = new JmriJFrame(rb.getString("VSDecoderPreferencesFrameTitle"));
	VSDecoderPreferencesPane tpP = new VSDecoderPreferencesPane(vsdecoderPrefs);
	vsdecoderPreferencesFrame.add(tpP);
	tpP.setContainer(vsdecoderPreferencesFrame);
	vsdecoderPreferencesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	vsdecoderPreferencesFrame.pack();
    }

    public void showVSDecoderPreferences() {
	if (vsdecoderPreferencesFrame == null) {
	    buildVSDecoderPreferencesFrame();
	}
	vsdecoderPreferencesFrame.setVisible(true);
	vsdecoderPreferencesFrame.requestFocus();
    }
	
    private String getNextVSDecoderID() {
	// vsdecoderID initialized to zero, pre-incremented before return...
	// first returned ID value is 1.
	return("IAD:VSD:VSDecoderID" + (++vsdecoderID));
    }

    // New version (now)
    public VSDecoder getVSDecoder(String profile_name) {
	VSDecoder vsd;
	String path;
	if (profiletable.containsKey(profile_name)) {
	    path = profiletable.get(profile_name);
	    log.debug("Profile " + profile_name + " is in table.  Path = " + path);
	    vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
	    decodertable.put(vsd.getID(), vsd);  // poss. broken for duplicate profile names
	    return(vsd);
	} else {
	    // Don't have enough info to try to load from file.
	    log.error("Requested profile not loaded: " + profile_name);
	    return(null);
	}
    }

    public VSDecoder getVSDecoder(String profile_name, String path) {
	VSDecoder vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
	decodertable.put(vsd.getID(), vsd); // poss. broken for duplicate profile names
	// If this profile_name is not in the list, update it.
	if (!(profiletable.containsKey(profile_name))) {
	    profiletable.put(profile_name, path);
	    fireMyEvent(new VSDManagerEvent(this, EventType.DECODER_LIST_CHANGE));
	}
	return(vsd);
    }

    public VSDecoder getVSDecoderByID(String id) {
	VSDecoder v = decodertable.get(id);
	if (v == null)
	    log.debug("No decoder in table! ID = " + id);
	return(decodertable.get(id));
    }

    @Deprecated
    public VSDecoder getVSDecoder(String profile_name, boolean create) {
	VSDecoder rv = null;

	// If the decoder is already in the hashmap, just return a pointer to it.
	if (decodertable.containsKey(profile_name)) {
		return(decodertable.get(profile_name));
	} else if (create) {
	    rv = new VSDecoder(getNextVSDecoderID(), profile_name);
	    if (!rv.isInitialized()) {
		return(null);
	    } else {
		decodertable.put(rv.getID(), rv);
		return(rv);
	    }
	} else {
	    // Not in hashmap.  Will need to load from file, or fail.
	    // For now, create a new one and add it to the table.
	    log.warn("Failed to find requested decoder: " + profile_name);
	    return(null);
	}
    } 

    public void setDefaultVSDecoder(VSDecoder d) {
	default_decoder = d;
    }

    public VSDecoder getDefaultVSDecoder() {
	return(default_decoder);
    }

    public ArrayList<String> getVSDProfileNames() {
	ArrayList<String> sl = new ArrayList<String>();
	for (String p : profiletable.keySet()) {
	    sl.add(p);
	}
	return(sl);
    }

    public Collection<VSDecoder> getVSDecoderList() {
	return(decodertable.values());
    }

    // VSDecoderManager Events
    public void addEventListener(VSDManagerListener listener) {
	listenerList.add(VSDManagerListener.class, listener);
    }

    public void removeEventListener(VSDManagerListener listener) {
	listenerList.remove(VSDManagerListener.class, listener);
    }

    void fireMyEvent(VSDManagerEvent evt) {
	//Object[] listeners = listenerList.getListenerList();

	for (VSDManagerListener l : listenerList.getListeners(VSDManagerListener.class)) {
	    l.eventAction(evt);
	}
    }

    public void loadProfiles(VSDFile vf) {
	Element root;
	if ((root = vf.getRoot()) == null)
	    return;

	List profiles = root.getChildren("profile");
	if ((profiles != null) && (profiles.size() > 0)) {
	    // New version: Create a profile name / file name map for each Profile
	    for (java.util.Iterator i = profiles.iterator(); i.hasNext();) {
		Element e = (Element) i.next();
		log.debug(e.toString());
		if (e.getAttributeValue("name") != null)
		    profiletable.put(e.getAttributeValue("name"), vf.getName());
	    }
	    
	    fireMyEvent(new VSDManagerEvent(this, EventType.DECODER_LIST_CHANGE));
	}
    }

    @Deprecated
    public void loadVSDProfiles(Element root, String path) {
	List profiles = root.getChildren("profile");
	if ((profiles != null) && (profiles.size() > 0)) {
	    // New version: Create a profile name / file name map for each Profile
	    for (java.util.Iterator i = profiles.iterator(); i.hasNext();) {
		Element e = (Element) i.next();
		log.debug(e.toString());
		if (e.getAttributeValue("name") != null)
		    profiletable.put(e.getAttributeValue("name"), path);
	    }
	    fireMyEvent(new VSDManagerEvent(this, EventType.DECODER_LIST_CHANGE));
	}
    }

    @Deprecated
    public void loadVSDecoders(Element root, VSDFile vf) {
	loadVSDecoders(vf);
    }

    public void loadVSDecoders(VSDFile vf) {
	Element root;
	if ((root = vf.getRoot()) == null)
	    return;

	List profiles = root.getChildren("profile");
	if ((profiles != null) && (profiles.size() > 0)) {
	    // Create a new VSDecoder object for each Profile in the XML file.
	    this.setDefaultVSDecoder(null);
	    for (java.util.Iterator i = profiles.iterator(); i.hasNext();) {
		Element e = (Element) i.next();
		log.debug(e.toString());
		VSDecoder vsd = this.getVSDecoder(e.getAttribute("name").getValue(), true);
		vsd.setXml(e, vf);
		decodertable.put(vsd.getID(), vsd);
		// Only set this as a default if it's the only (or first) one in the file.
		if (vsd.isDefault() && (this.getDefaultVSDecoder() == null)) {
		    setDefaultVSDecoder(vsd);
		}
	    }
	    fireMyEvent(new VSDManagerEvent(this, EventType.DECODER_LIST_CHANGE));
	}
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderManager.class.getName());

}