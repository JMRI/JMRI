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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.util.ResourceBundle;

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
	this(rb.getString("LoadVSDFileChoserTitle")); // Shouldn't this be in the resource bundle?
    }
    
    JFileChooser fileChooser;
    static private String last_path = null;
    
    /**
     *  The action is performed. Let the user choose the file to load from. Read
     *  XML for each VSDecoder Profile.
     *
     * @param  e  The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
	if (fileChooser == null) {
	    // Need to somehow give the user a history...
	    // Must investigate JFileChooser...
	    String start_dir = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
	    if (last_path != null)
		start_dir = last_path;

	    log.debug("Using path: " + start_dir);

	    fileChooser = new JFileChooser(start_dir);
	    jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter(rb.getString("LoadVSDFileChooserFilterLabel"));
	    filt.addExtension("vsd");
	    filt.addExtension("zip");
	    fileChooser.setFileFilter(filt);
	    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    fileChooser.setCurrentDirectory(new File(start_dir));
	}
	int retVal = fileChooser.showOpenDialog(null);
	if (retVal != JFileChooser.APPROVE_OPTION) {
	    return;
	    // give up if no file selected
	}

	loadVSDFile(fileChooser.getSelectedFile());

	// Store the last used directory
	try {
	    last_path = fileChooser.getCurrentDirectory().getCanonicalPath();
	} catch (java.io.IOException err) {
	    log.debug("Error getting current directory: " + err);
	    last_path = VSDecoderManager.instance().getVSDecoderPreferences().getDefaultVSDFilePath();
	}
    }
    
    
    public static boolean loadVSDFile(java.io.File f) {
	VSDFile vsdfile;
	// Create a VSD (zip) file.
	try {
	    vsdfile = new VSDFile(f);
	    log.debug("VSD File name = " + vsdfile.getName());
	    if (vsdfile.isInitialized()) {	
		VSDecoderManager.instance().loadProfiles(vsdfile);
	    }
	    // Cleanup and close files.
	    vsdfile.close();

	    if (!vsdfile.isInitialized()) {
		JOptionPane.showMessageDialog(null, vsdfile.getStatusMessage(), 
					      rb.getString("VSDFileError"), JOptionPane.ERROR_MESSAGE);
	    }

	    return(vsdfile.isInitialized());

	} catch (java.util.zip.ZipException ze) {
	    log.error("ZipException opening file " + f.toString(), ze);
	    return(false);
	} catch (java.io.IOException ze) {
	    log.error("IOException opening file " + f.toString(), ze);
	    return(false);
	}
    }
    
    public static boolean loadVSDFile(String fp) {
	VSDFile vsdfile;

	try {
	    // Create a VSD (zip) file.
	    vsdfile = new VSDFile(fp);
	    log.debug("VSD File name = " + vsdfile.getName());
	    if (vsdfile.isInitialized()) {	
	    VSDecoderManager.instance().loadProfiles(vsdfile);
	}
	    // Cleanup and close files.
	    vsdfile.close();
	    
	    return(vsdfile.isInitialized());
	} catch (java.util.zip.ZipException ze) {
	    log.error("ZipException opening file " + fp, ze);
	    return(false);
	} catch (java.io.IOException ze) {
	    log.error("IOException opening file " + fp, ze);
	    return(false);
	}

	/*
	File f = null;
	try {
	    f = new File(fp);
	    return(loadVSDFile(f));
	} catch (java.io.IOException ioe) {
	    log.warn("IO Error auto-loading VSD File: " + (f==null?"(null)":f.getAbsolutePath()) + " ", ioe);
	    return(false);
	} catch (NullPointerException npe) {
	    log.warn("NP Error auto-loading VSD File: FP = " + fp, npe);
	    return(false);
	}
	*/
    }

	// initialize logging
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoadVSDFileAction.class.getName());

}
