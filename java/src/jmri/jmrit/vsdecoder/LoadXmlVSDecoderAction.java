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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.XmlFile;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import java.util.List;
import java.util.ResourceBundle;

import org.jdom.Element;


/**
 *  Load VSDecoder Profiles from XML
 *
 * Adapted from LoadXmlThrottleProfileAction
 * by Glen Oberhauser (2004)
 *
 * @author     Mark Underwood 2011
 * @version     $Revision$
 */
@SuppressWarnings("serial")
public class LoadXmlVSDecoderAction extends AbstractAction {
    static final ResourceBundle rb = VSDecoderBundle.bundle();
    
    /**
     *  Constructor
     *
     * @param  s  Name for the action.
     */
    public LoadXmlVSDecoderAction(String s) {
	super(s);
	// Pretty sure I don't need this
	// disable the ourselves if there is no throttle Manager
	/*
	if (jmri.InstanceManager.throttleManagerInstance() == null) {
	    setEnabled(false);
	}
	*/
    }
    
    public LoadXmlVSDecoderAction() {
	this("Load VSDecoder Profile"); // Shouldn't this be in the resource bundle?
    }
    
    JFileChooser fileChooser;
    
    /**
     *  The action is performed. Let the user choose the file to load from. Read
     *  XML for each VSDecoder Profile.
     *
     * @param  e  The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
	if (fileChooser == null) {
	    fileChooser = jmri.jmrit.XmlFile.userFileChooser(rb.getString("PromptXmlFileTypes"), "xml");
	    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    fileChooser.setCurrentDirectory(new File(VSDecoderPane.getDefaultVSDecoderFolder()));
	}
	int retVal = fileChooser.showOpenDialog(null);
	if (retVal != JFileChooser.APPROVE_OPTION) {
	    return;
	    // give up if no file selected
	}

	try {
	    loadVSDecoderProfile(fileChooser.getSelectedFile());
	} catch (java.io.IOException e1) {
	    log.warn("Exception while reading file", e1);
	}
    }
    
    /**
     *  Parse the XML file and create ThrottleFrames.
     *  Returns true if throttle loaded successfully.
     *
     * @param  f  The XML file containing throttles.
     */
    @SuppressWarnings("unchecked")
	public boolean loadVSDecoderProfile(java.io.File f) throws java.io.IOException {
	try {
	    VSDecoderPrefs prefs = new VSDecoderPrefs();
	    Element root = prefs.rootFromFile(f);

	    // WARNING: This may be out of sync with the Store... the root element is <VSDecoderConfig>
	    // not sure, must investigate.  See what XmlFile.rootFromFile(f) does...
	    List<Element> profiles = root.getChildren("VSDecoder");
	    if ((profiles != null) && (profiles.size() > 0)) {
		// Create a new VSDecoder object for each Profile in the XML file.
		for (java.util.Iterator<Element> i = profiles.iterator(); i.hasNext();) {
		    Element e = i.next();
		    log.debug(e.toString());
		    //VSDecoder vsd = VSDecoderManager.instance().getVSDecoder(e.getAttribute("name").getValue(), f.getPath());
		}
	    }

	} catch (org.jdom.JDOMException ex) {
	    log.warn("Loading VSDecoder Profile exception",ex);
	    return false;
	}
	return true;
    }
    
	/**
	 * An extension of the abstract XmlFile. No changes made to that class.
	 * 
	 * @author glen
	 * @version $Revision$
	 */
	static class VSDecoderPrefs extends XmlFile {}

	// initialize logging
	static Logger log = LoggerFactory.getLogger(LoadXmlVSDecoderAction.class.getName());

}
