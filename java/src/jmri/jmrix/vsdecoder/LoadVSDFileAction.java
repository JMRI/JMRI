package jmri.jmrix.vsdecoder;

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

import jmri.jmrit.XmlFile;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.util.NoArchiveFileFilter;
import jmri.util.FileChooserFilter;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.io.IOException;


import org.jdom.Element;

import java.util.zip.*;


/**
 *  Load VSDecoder Profiles from XML
 *
 * Adapted from LoadXmlThrottleProfileAction
 * by Glen Oberhauser (2004)
 *
 * @author     Mark Underwood 2011
 * @version     $Revision$
 */
public class LoadVSDFileAction extends AbstractAction {
    static final ResourceBundle rb = VSDecoderBundle.bundle();
    
    /**
     *  Constructor
     *
     * @param  s  Name for the action.
     */
    public LoadVSDFileAction(String s) {
	super(s);
    }
    
    public LoadVSDFileAction() {
	this("Load VSD File"); // Shouldn't this be in the resource bundle?
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
	    String default_dir = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
	    log.debug("Default path: " + default_dir);
	    fileChooser = new JFileChooser(VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath());
	    jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("VSD Files");
	    filt.addExtension("vsd");
	    filt.addExtension("zip");
	    fileChooser.setFileFilter(filt);
	    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    fileChooser.setCurrentDirectory(new File(VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath()));
	}
	int retVal = fileChooser.showOpenDialog(null);
	if (retVal != JFileChooser.APPROVE_OPTION) {
	    return;
	    // give up if no file selected
	}

	try {
	    loadVSDFile(fileChooser.getSelectedFile());
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
	public static boolean loadVSDFile(java.io.File f) throws java.io.IOException {

	VSDecoderPrefs prefs = new VSDecoderPrefs();

	try {
	    // Create a VSD (zip) file.
	    VSDFile vsdfile = new VSDFile(f);
	    log.debug("VSD File name = " + vsdfile.getName());
	    
	    // Debug: List all the top-level contents in the file.
	    Enumeration entries = vsdfile.entries();
	    while(entries.hasMoreElements()) {
		ZipEntry z = (ZipEntry)entries.nextElement();
		log.debug("Entry: " + z.getName());
	    }

	    // Look up the config.xml file
	    ZipEntry config = vsdfile.getEntry(rb.getString("VSD_XMLFileName"));
	    log.debug("ZipEntry : " + config.getName());

	    // Get the root XML element from the config.xml file
	    File f2 = new File(vsdfile.getURL(rb.getString("VSD_XMLFileName")));
	    log.debug("Extracted file : " + f2.getPath());
	    Element root = prefs.rootFromFile(f2);

	    // WARNING: This may be out of sync with the Store... the root element is <VSDecoderConfig>
	    // not sure, must investigate.  See what XmlFile.rootFromFile(f) does...

	    VSDecoderManager.instance().loadVSDecoders(root, vsdfile);

	    // Cleanup and close files.
	    vsdfile.close();

	} catch (org.jdom.JDOMException ex) {
	    log.warn("Loading VSDecoder Profile exception",ex);
	    return false;
	} catch (java.util.zip.ZipException ex) {
	    log.warn("Loading VSD File exception", ex);
	    return false;
	}
	return true;
    }
    
	/**
	 * An extension of the abstract XmlFile. No changes made to that class.
	 * 
	 * @author glen
	 * @version $Revision: 1.6 $
	 */
	static class VSDecoderPrefs extends XmlFile {}

    public static boolean loadVSDFile(String fp, String fn) {
	return(loadVSDFile(fp + File.separator + fn));
    }

    public static boolean loadVSDFile(String fp) {
	File f = null;
	try {
	    f = new File(fp);
	    return(loadVSDFile(f));
	} catch (java.io.IOException ioe) {
	    log.warn("IO Error auto-loading VSD File: " + f.getAbsolutePath() + " ", ioe);
	    return(false);
	} catch (NullPointerException npe) {
	    log.warn("NP Error auto-loading VSD File: FP = " + fp, npe);
	    return(false);
	}

    }

	// initialize logging
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoadXmlVSDecoderAction.class.getName());

}
