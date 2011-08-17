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

    public static enum EventType { NONE, DECODER_LIST_CHANGE };

    private static final ResourceBundle vsdBundle = VSDecoderBundle.bundle();

    HashMap<String, VSDecoder> decodertable;

    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    private static VSDecoderManager instance = null;
    private static VSDecoderManagerThread thread = null;

    private VSDecoderPreferences vsdecoderPrefs;
    private JmriJFrame vsdecoderPreferencesFrame;

    private VSDecoder default_decoder = null;

    // Can only be called from static instance()
    public VSDecoderManager() {
	// Setup the decoder table
	decodertable = new HashMap<String, VSDecoder>();
	// Get preferences
	String dirname = XmlFile.prefsDir()+ "vsdecoder" +File.separator;
	XmlFile.ensurePrefsPresent(dirname);
	vsdecoderPrefs = new VSDecoderPreferences(dirname+ "VSDecoderPreferences.xml");
    }

    public static VSDecoderManager instance() {
	if (thread == null) {
	    thread = VSDecoderManagerThread.instance(true);
	}
	return(thread.manager());

	/*
	if (instance == null)
	    {
		instance = new VSDecoderManager();
	    }
	return instance;
	*/
    }

    public VSDecoderPreferences getVSDecoderPreferences() {
	return(vsdecoderPrefs);
    }

	private void buildVSDecoderPreferencesFrame() {
		vsdecoderPreferencesFrame = new JmriJFrame(vsdBundle.getString("VSDecoderPreferencesFrameTitle"));
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
	

    public VSDecoder getVSDecoder(String profile_name) {
	return(getVSDecoder(profile_name, false));
    }

    public VSDecoder getVSDecoder(String profile_name, boolean create) {
	VSDecoder rv = null;

	// If the decoder is already in the hashmap, just return a pointer to it.
	if (decodertable.containsKey(profile_name)) {
		return(decodertable.get(profile_name));
	} else if (create) {
	    try {
		rv = new VSDecoder(profile_name);
	    } catch(jmri.AudioException e) {
		return(null);
	    }
	    decodertable.put(profile_name, rv);
	    return(rv);

	} else {
	    // Not in hashmap.  Will need to load from file, or fail.
	    // For now, create a new one and add it to the table.
	    log.warn("Failed to find requested decoder: " + profile_name);
	    return(null);
	}
    } 

    public void addVSDecoder(String name, VSDecoder vsd) {
	this.addVSDecoder(name, vsd, false);
    }

    public void addVSDecoder(String name, VSDecoder vsd, boolean create) {
	decodertable.put(name, vsd);
	fireMyEvent(new VSDManagerEvent(this, EventType.DECODER_LIST_CHANGE));
    }

    public void setDefaultVSDecoder(VSDecoder d) {
	default_decoder = d;
    }

    public VSDecoder getDefaultVSDecoder() {
	return(default_decoder);
    }

    public ArrayList<String> getVSDProfileNames() {
	ArrayList<String> sl = new ArrayList<String>();
	for (VSDecoder v : decodertable.values()) {
	    sl.add(v.getProfileName());
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
	Object[] listeners = listenerList.getListenerList();

	for (VSDManagerListener l : listenerList.getListeners(VSDManagerListener.class)) {
	    l.eventAction(evt);
	}
    }

    public void loadVSDecoders(Element root, VSDFile vf) {
	List<Element> profiles = root.getChildren("vsdecoder");
	if ((profiles != null) && (profiles.size() > 0)) {
	    // Create a new VSDecoder object for each Profile in the XML file.
	    this.setDefaultVSDecoder(null);
	    for (java.util.Iterator<Element> i = profiles.iterator(); i.hasNext();) {
		Element e = i.next();
		log.debug(e.toString());
		VSDecoder vsd = this.getVSDecoder(e.getAttribute("name").getValue(), true);
		vsd.setXml(e, vf);
		// Only set this as a default if it's the only (or first) one in the file.
		if (vsd.isDefault() && (this.getDefaultVSDecoder() == null)) {
		    setDefaultVSDecoder(vsd);
		}
	    }
	}
	fireMyEvent(new VSDManagerEvent(this, EventType.DECODER_LIST_CHANGE));
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderManager.class.getName());

}