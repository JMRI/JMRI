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
import java.io.File;
import java.util.ArrayList;
import jmri.util.PhysicalLocation;

import org.jdom.Document;
import org.jdom.Element;

import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;

public class VSDecoderPreferences {
    // Private variables to hold preference values
    private boolean _autoStartEngine = false; // play engine sound w/o waiting for "Engine Start" button pressed.
    private String _defaultVSDFilePath = null;
    private String _defaultVSDFileName = null;
    private boolean _autoLoadDefaultVSDFile = false; // Automatically load a VSD file.
    private PhysicalLocation _listenerPosition;

    // Other internal variables
    //private Dimension _winDim = new Dimension(800,600);
    private String prefFile;
    private ArrayList<PropertyChangeListener> listeners;
    
    public VSDecoderPreferences(String sfile)
    {
    	prefFile = sfile;
	VSDecoderPrefsXml prefs = new VSDecoderPrefsXml();
	File file = new File(prefFile );
	Element root;
	try {
	    root = prefs.rootFromFile(file);
        } catch (java.io.FileNotFoundException e2) {
	    // Set default values
	    _defaultVSDFilePath = FileUtil.getExternalFilename("program:resources/vsdecoder");
	    _defaultVSDFileName = "example.vsd";
	    _listenerPosition = new PhysicalLocation(); // default to 0, 0, 0
            log.info("Did not find VSDecoder preferences file.  This is normal if you haven't save the preferences before");
            root = null;
	} catch (Exception e) {
	    log.error("Exception while loading VSDecoder preferences: " + e);
	    root = null;
	}
	if (root != null)
	    load(root.getChild("VSDecoderPreferences"));
    }
    
    public VSDecoderPreferences() {   }
    
    public void load(org.jdom.Element e)
    {
    	if (e==null) return;
    	org.jdom.Attribute a;
	org.jdom.Element c;
    	if ((a = e.getAttribute("isAutoStartingEngine")) != null )  setAutoStartEngine( a.getValue().compareTo("true") == 0 );
    	if ((a = e.getAttribute("isAutoLoadingDefaultVSDFile")) != null )  setAutoLoadDefaultVSDFile( a.getValue().compareTo("true") == 0 );
	if ((c = e.getChild("DefaultVSDFilePath")) != null) setDefaultVSDFilePath(c.getValue());
	if ((c = e.getChild("DefaultVSDFileName")) != null) setDefaultVSDFileName(c.getValue());
	if ((c = e.getChild("ListenerPosition")) != null)
	    setListenerPosition(c.getValue());
	else
	    setListenerPosition(new PhysicalLocation());
    }
    
    /**
	 * An extension of the abstract XmlFile. No changes made to that class.
	 * 
	 */
	static class VSDecoderPrefsXml extends XmlFile { }
	
    private org.jdom.Element store() {
	org.jdom.Element ec;
    	org.jdom.Element e = new org.jdom.Element("VSDecoderPreferences");
	e.setAttribute("isAutoStartingEngine", ""+isAutoStartingEngine());
	e.setAttribute("isAutoLoadingDefaultVSDFile", ""+isAutoLoadingDefaultVSDFile());
	ec = new Element("DefaultVSDFilePath");
	ec.setText("" + getDefaultVSDFilePath());
	e.addContent(ec);
	ec = new Element("DefaultVSDFileName");
	ec.setText("" + getDefaultVSDFileName());
	e.addContent(ec);
	ec = new Element("ListenerPosition");
	ec.setText("" + _listenerPosition.toString());
	e.addContent(ec);
    	return e;
    }

    public void set(VSDecoderPreferences tp)
    {
    	setAutoStartEngine (tp.isAutoStartingEngine() );
    	setAutoLoadDefaultVSDFile (tp.isAutoLoadingDefaultVSDFile() );
    	setDefaultVSDFilePath(tp.getDefaultVSDFilePath() );
    	setDefaultVSDFileName(tp.getDefaultVSDFileName() );
	setListenerPosition(tp.getListenerPosition());
    	
    	if (listeners != null)
    		for (int i = 0; i < listeners.size(); i++) {
    			PropertyChangeListener l = listeners.get(i);
    			PropertyChangeEvent e = new PropertyChangeEvent(this, "VSDecoderPreferences", null, this );
    			l.propertyChange(e);
    		}
    }
    
    public boolean compareTo(VSDecoderPreferences tp)
    {
    	return( isAutoStartingEngine() != tp.isAutoStartingEngine() ||
		isAutoLoadingDefaultVSDFile() != tp.isAutoLoadingDefaultVSDFile() ||
		!(getDefaultVSDFilePath().equals(tp.getDefaultVSDFilePath())) ||
		!(getDefaultVSDFileName().equals(tp.getDefaultVSDFileName())) ||
		!(getListenerPosition().equals(tp.getListenerPosition()))
		);
    }
    
    public void save() {
    	if (prefFile == null)
    		return;
    	XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
    	xf.makeBackupFile(prefFile );
    	File file=new File(prefFile );
    	try {
    		//The file does not exist, create it before writing
    		File parentDir=file.getParentFile();
    		if(!parentDir.exists())
    			if (!parentDir.mkdir()) // make directory, check result
    			    log.error("failed to make parent directory");
    		if (!file.createNewFile()) // create file, check result
    		    log.error("createNewFile failed");
    	} catch (Exception exp) {
    		log.error("Exception while writing the new VSDecoder preferences file, may not be complete: "+exp);
    	}

    	try {
    		Element root = new Element("vsdecoder-preferences");
    		//Document doc = XmlFile.newDocument(root, XmlFile.dtdLocation+"vsdecoder-preferences.dtd");
    		Document doc = XmlFile.newDocument(root);
    		// add XSLT processing instruction
    		// <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
/*TODO    		java.util.Map<String,String> m = new java.util.HashMap<String,String>();
    		m.put("type", "text/xsl");
    		m.put("href", jmri.jmrit.XmlFile.xsltLocation+"throttles-preferences.xsl");
    		ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
    		doc.addContent(0,p);*/
    		root.setContent( store() );
    		xf.writeXML(file, doc);
    	}       
    	catch (Exception ex){
    		log.warn("Exception in storing vsdecoder preferences xml: "+ex);
    	}
    }
    
    public String getDefaultVSDFilePath() {
	return(_defaultVSDFilePath);
    }

    public void setDefaultVSDFilePath(String s) {
	_defaultVSDFilePath = s;
    }
    
    public String getDefaultVSDFileName() {
	return(_defaultVSDFileName);
    }

    public void setDefaultVSDFileName(String s) {
	_defaultVSDFileName = s;
    }

    public boolean isAutoStartingEngine() {
	return(_autoStartEngine);
    }

    public void setAutoStartEngine(boolean b) {
	_autoStartEngine = b;
    }

    public boolean isAutoLoadingDefaultVSDFile() {
	return(_autoLoadDefaultVSDFile);
    }

    public void setAutoLoadDefaultVSDFile(boolean b) {
	_autoLoadDefaultVSDFile = b;
    }

    public PhysicalLocation getListenerPosition() {
	log.debug("getListenerPosition() : " + _listenerPosition.toString());
	return(_listenerPosition);
    }

    public void setListenerPosition(PhysicalLocation p) {
	_listenerPosition = p;
    }

    public void setListenerPosition(String pos) {
	PhysicalLocation p = PhysicalLocation.parse(pos);
	if (p != null) {
	    this.setListenerPosition(p);
	} else {
	    this.setListenerPosition(new PhysicalLocation());
	}
    }

    /**
     * Add an AddressListener. AddressListeners are notified when the user
     * selects a new address and when a Throttle is acquired for that address
     * 
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
	if (listeners == null)
	    listeners = new ArrayList<PropertyChangeListener>(2);		
	if (!listeners.contains(l)) 
	    listeners.add(l);
    }
    
    /**
     * Remove an AddressListener. 
     * 
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
	if (listeners == null) 
	    return;
	if (listeners.contains(l)) 
	    listeners.remove(l);		
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoderPreferences.class.getName());
}
