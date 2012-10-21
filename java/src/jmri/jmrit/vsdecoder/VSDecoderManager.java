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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFrame;
import org.jdom.Element;

import jmri.DccLocoAddress;
import jmri.jmrit.XmlFile;
import jmri.util.JmriJFrame;
import jmri.util.PhysicalLocation;
import jmri.Reporter;
import jmri.LocoAddress;
import jmri.PhysicalLocationReporter;

// VSDecoderFactory
//
// Builds VSDecoders as needed.  Handles loading from XML if needed.

class VSDecoderManager implements PropertyChangeListener {

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

    // Unused?
    //private PhysicalLocation listener_position;

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

	// Listen to ReporterManager for Report List changes
	setupReporterManagerListener();
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
	vsdecoderPreferencesFrame.pack();
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
	this.loadProfiles(path);
	VSDecoder vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
	decodertable.put(vsd.getID(), vsd); // poss. broken for duplicate profile names
	return(vsd);
    }

    public VSDecoder getVSDecoderByID(String id) {
	VSDecoder v = decodertable.get(id);
	if (v == null)
	    log.debug("No decoder in table! ID = " + id);
	return(decodertable.get(id));
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

    public void setDecoderPositionByID(String id, PhysicalLocation p) {
	VSDecoder d = decodertable.get(id);
	if (d != null)
	    d.setPosition(p);
    }

    public void setDecoderPositionByAddr(LocoAddress a, PhysicalLocation p) {
	// Find the addressed decoder
	// This is a bit hokey.  Need a better way to index decoder by address
	// OK, this whole LocoAddress vs. DccLocoAddress thing has rendered this SUPER HOKEY.
	DccLocoAddress da = new DccLocoAddress(a.getNumber(), a.getProtocol());
	DccLocoAddress dd = null;
	log.debug("Decoder Address: " + a.getNumber());
	for ( VSDecoder d : decodertable.values()) {
	    dd = new DccLocoAddress(d.getAddress().getNumber(), d.getAddress().getProtocol());
	    if (da.equals(dd)) {
		d.setPosition(p);
		return;
	    }
	}
	// decoder not found.  Do nothing.
	return;
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

    private void loadProfiles(String path) {
	try {
	    VSDFile vsdfile = new VSDFile(path);
	    if (vsdfile.isInitialized()) {
		this.loadProfiles(vsdfile);
	    }
	} catch (java.util.zip.ZipException e) {
	    log.error("ZipException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	} catch (java.io.IOException ioe) {
	    log.error("IOException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	}
    }

    // This listener listens to the ReporterManager for changes to the list of Reporters.
    // Need to trap list length (name="length") changes and add listeners when new ones are added.
    void setupReporterManagerListener() {
	// Register ourselves as a listener for changes to the Reporter list.  For now, we won't do this. Just force a
	// save and reboot after reporters are added.  We'll fix this later.
	jmri.InstanceManager.reporterManagerInstance().addPropertyChangeListener(new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
		    log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
		    reporterManagerPropertyChange(event);
		}
	    });

	// Now, the Reporter Table might already be loaded and filled out, so we need to get all the Reporters and list them.
	// And add ourselves as a listener to them.
	List<String> names = jmri.InstanceManager.reporterManagerInstance().getSystemNameList();
	Iterator<String> i = names.iterator();
	while (i.hasNext()) {
	    Reporter r = jmri.InstanceManager.reporterManagerInstance().getBySystemName(i.next());
	    if (r != null) {
		r.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
			    log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
			    reporterPropertyChange(event);
			}
		    });
		  
	    }
	}
    }

    public void propertyChange(PropertyChangeEvent evt) {
	// does nothing ... yet...
	return;
    }

    private Boolean isLocoNetEntryReport(String s) {
	Pattern ln_p = Pattern.compile("(\\d+) enter");  // Match a number followed by the word "enter".  This is the LocoNet pattern.

	Matcher m = ln_p.matcher(s);
	if (m.find()) {
	    return(true);
	} else {
	    return(false);
	}
    }

    private String getLnReportAddress(String s) {
	Pattern ln_p = Pattern.compile("(\\d+) enter");  // Match a number followed by the word "enter".  This is the LocoNet pattern.

	Matcher m = ln_p.matcher(s);
	if (m.find()) {
	    return(m.group(1));
	} else {
	    return(null);
	}
    }

    
    public void reporterPropertyChange(PropertyChangeEvent event) {
	// Needs to check the ID on the event, look up the appropriate VSDecoder,
	// get the location of the event source, and update the decoder's location.
	String sa;
	Integer la = 0;
	String eventName = (String)event.getPropertyName();
	String newValue = (String)event.getNewValue();

	if (eventName.equals("currentReport")) {
	    Reporter arp = (Reporter) event.getSource();
	    // Need to decide which reporter it is, so we can use different methods
	    // to extract the address and the location.
	    PhysicalLocation loc = PhysicalLocation.getBeanPhysicalLocation(arp);
	    if (loc.equals(PhysicalLocation.Origin)) {
		// Physical location at origin means it hasn't been set.
		return;
	    }
	    //if (arp.supportsPhysicalLocation()) { 
	    if (arp instanceof PhysicalLocationReporter) {
		if (((PhysicalLocationReporter)arp).getDirection(newValue) == PhysicalLocationReporter.Direction.ENTER)
		    setDecoderPositionByAddr(((PhysicalLocationReporter)arp).getLocoAddress(newValue), loc);
	    } // Reporting object implements PhysicalLocationReporter
	    else {
		log.debug("Reporter doesn't support physical location reporting.");
	    }
	} // name == currentReport
	return;
    }

    public void reporterManagerPropertyChange(PropertyChangeEvent event) {
	String eventName = event.getPropertyName();
	String sOldValue, sNewValue;
	Integer oldValue, newValue;

	log.debug("VSDecoder received Reporter Manager Property Change: " + eventName);
	if (eventName.equals("length")) {
	    log.debug("New Reporter added to list.");
	    
	}
    }

    public void loadProfiles(VSDFile vf) {
	Element root;
	String pname;
	if ((root = vf.getRoot()) == null)
	    return;
	
	ArrayList<String> new_entries = new ArrayList<String>();

	//List<Element> profiles = root.getChildren("profile");
	@SuppressWarnings("unchecked")
	java.util.Iterator<Element> i = root.getChildren("profile").iterator();
	while (i.hasNext()) {
	    Element e = i.next();
	    log.debug(e.toString());
	    if ((pname = e.getAttributeValue("name")) != null) {
		profiletable.put(pname, vf.getName());
		new_entries.add(pname);
	    }
	}

	// debug
	/*
	for (String s : new_entries) {
	    log.debug("New entry: " + s);
	}
	*/
	// /debug
	    
	fireMyEvent(new VSDManagerEvent(this, EventType.DECODER_LIST_CHANGE, new_entries));
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderManager.class.getName());

}