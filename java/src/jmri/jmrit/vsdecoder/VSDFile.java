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

import java.util.zip.*;
import java.io.*;
import org.jdom.Element;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import jmri.jmrit.XmlFile;

public class VSDFile extends ZipFile {
    
    // Dummy class just used to instantiate
    private static class VSDXmlFile extends XmlFile { }

    static final ResourceBundle rb = VSDecoderBundle.bundle();

    protected Element root;
    protected boolean initialized = false;

    ZipInputStream zis;

    public VSDFile(File file) throws ZipException, IOException {
	super(file);
	initialized = init();
    }

    public VSDFile(File file, int mode) throws ZipException, IOException {
	super(file, mode);
	initialized = init();
    }

    public VSDFile(String name) throws ZipException, IOException {
	super(name);
	initialized = init();
    }

    public boolean isInitialized() { return(initialized); }

    protected boolean init() {
	VSDXmlFile xmlfile = new VSDXmlFile();
	String path = rb.getString("VSD_XMLFileName");

	try {
	    // Debug: List all the top-level contents in the file.
	    @SuppressWarnings("rawtypes")
	    Enumeration entries = this.entries();
	    while(entries.hasMoreElements()) {
		ZipEntry z = (ZipEntry)entries.nextElement();
		log.debug("Entry: " + z.getName());
	    }

	    ZipEntry config = this.getEntry(path);
	    if (config == null) {
		log.error("File does not contain " + path);
		return(false);
	    }
	    File f2 = new File(this.getURL(path));
	    root = xmlfile.rootFromFile(f2);
	    Boolean rv = this.validate(root);
	    if (!rv) {
		log.error("VALIDATE FAILED: File " + path);
	    }
	    return(rv);

	} catch (java.io.IOException ioe) {
	    log.warn("IO Error auto-loading VSD File: " + path + " ", ioe);
	    return(false);
	} catch (NullPointerException npe) {
	    log.warn("NP Error auto-loading VSD File: path = " + path, npe);
	    return(false);
	} catch (org.jdom.JDOMException ex) {
	    log.error("JDOM Exception loading VSDecoder from path " + path, ex);
	    return(false);
	}
    }

    public Element getRoot() { return(root); }

    public java.io.InputStream getInputStream(String name) {
	
	java.io.InputStream rv;
	try {
	    ZipEntry e = this.getEntry(name);
	    if (e == null) {
		e = this.getEntry(name.toLowerCase());
		if (e == null) {
		    e = this.getEntry(name.toUpperCase());
		    if (e == null) {
			// I give up.  Return null.
			return(null);
		    }
		}
	    }
	    rv = getInputStream(this.getEntry(name));
	} catch (IOException e) {
	    log.error("IOException caught " + e);
	    rv = null;
	} catch(NullPointerException ne) {
	    log.error("Null Pointer Exception caught. name=" +name, ne);
	    rv = null;
	}
    return(rv);
    }

    public java.io.File getFile(String name) {
	try {
	    ZipEntry e = this.getEntry(name);

	    File f = new File(e.getName());
	    return(f);
	} catch (NullPointerException e) {
	    return(null);
	}

    }

    public String getURL(String name) {
	try {
	    // Grab the entry from the Zip file, and create a tempfile to dump it into
	    ZipEntry e = this.getEntry(name);
	    File t = File.createTempFile(name, ".wav.tmp");
	    t.deleteOnExit();

	    // Dump the file from the Zip into the tempfile.
	    copyInputStream(this.getInputStream(e), new BufferedOutputStream(new FileOutputStream(t)));
	    
	    // return the name of the tempfile.
	    return(t.getPath());
	    
	} catch (NullPointerException e) {
	    log.warn("Null pointer exception", e);
	    return(null);
	} catch (IOException e) {
	    log.warn("IO exception", e);
	    return(null);
	}
    }

    private static final void copyInputStream(InputStream in, OutputStream out)
	throws IOException
    {
	byte[] buffer = new byte[1024];
	int len;
	
	while((len = in.read(buffer)) >= 0)
	    out.write(buffer, 0, len);
	
	in.close();
	out.close();
    }
    
    
    public boolean validate(Element xmlroot) {
	Element e, el;
	// Iterate through all the profiles in the file.
	// Would like to get rid of this suppression, but I think it's fairly safe to assume a list of children
	// returned from an Element is going to be a list of Elements.
	@SuppressWarnings("unchecked")
	Iterator<Element> i = xmlroot.getChildren("profile").iterator();
	// If no Profiles, file is invalid.
	if (!i.hasNext()) {
	    log.warn("Validate: No Profiles.");
	    return(false);
	}

	// Iterate through Profiles
	while (i.hasNext()) {
	    e = i.next(); // e points to a profile.
	    log.debug("Validate: Profile " + e.getAttributeValue("name"));
	    // Get the "Sound" children ... these are the ones that should have files
	    // Would like to get rid of this suppression, but I think it's fairly safe to assume a list of children
	    // returned from an Element is going to be a list of Elements.
	    @SuppressWarnings("unchecked")
	    Iterator<Element> i2 = (e.getChildren("sound")).iterator();
	    if (!i2.hasNext()) {
		log.warn("Validate: Profile " + e.getAttributeValue("name") + " has no Sounds");
		return(false);
	    }

	    // Iterate through Sounds
	    while (i2.hasNext()) {
		el = i2.next();
		log.debug("Element: " + el.toString());
		if (el.getAttribute("name") == null) {
		    log.debug("Name missing.");
		    return(false);
		}
		String type = el.getAttributeValue("type");
		log.debug("  Name: " + el.getAttributeValue("name"));
		log.debug("   type: " + type);
		if (type.equals("configurable")) {
		    // Validate a Configurable Sound
		    // All these elements are optional, so if the element is missing,
		    // that's OK.  But if there is an element, and the FILE is missing,
		    // that's bad.
		    if (!validateOptionalFile(el, "start-file")) {
			return(false);
		    }
		    if (!validateOptionalFile(el, "mid-file")) {
			return(false);
		    }
		    if (!validateOptionalFile(el, "end-file")) {
			return(false);
		    }
		    if (!validateOptionalFile(el, "short-file")) {
			return(false);
		    }
		} else if (type.equals("diesel")) {
		    // Validate a Diesel sound
		    // All these elements are optional, so if the element is missing,
		    // that's OK.  But if there is an element, and the FILE is missing,
		    // that's bad.
		    String[] file_elements = {"file"};
		    if (!validateOptionalFile(el, "start-file")) {
			return(false);
		    }
		    if (!validateOptionalFile(el, "shutdown-file")) {
			return(false);
		    }
		    if (!validateFiles(el, "notch-sound",file_elements)) {
			return(false);
		    }
		    if (!validateFiles(el, "notch-transition", file_elements)) {
			return(false);
		    }
		} else if (type.equals("diesel2")) {
		    // Validate a diesel2 type sound
		    // Validate a Diesel sound
		    // All these elements are optional, so if the element is missing,
		    // that's OK.  But if there is an element, and the FILE is missing,
		    // that's bad.
		    String[] file_elements = {"file", "accel-file", "decel-file"};
		    if (!validateOptionalFile(el, "start-file")) {
			return(false);
		    }
		    if (!validateOptionalFile(el, "shutdown-file")) {
			return(false);
		    }
		    if (!validateFiles(el, "notch-sound", file_elements)) {
			return(false);
		    }
		} else if (type.equals("steam")) {
		    // Validate a Steam sound
		    String[] file_elements = {"file"};
		    if (!validateRequiredElement(el, "top-speed")) {
			return(false);
		    }
		    if (!validateRequiredElement(el, "driver-diameter")) {
			return(false);
		    }
		    if (!validateRequiredElement(el, "cylinders")) {
			return(false);
		    }
		    if (!validateRequiredElement(el, "rpm-steps")) {
			return(false);
		    }
		    if (!validateFiles(el, "rpm-step", file_elements)) {
			return(false);
		    }
		}
	    }
	}
	log.debug("File Validation Successful.");
	return(true);
    }

    protected boolean validateRequiredElement(Element el, String name) {
	if (el.getChild(name) == null) {
	    log.debug("Element " + name + " for Element " + el.getAttributeValue("name") + " missing.");
	    return(false);
	}
	return(true);
    }
    
    protected boolean validateOptionalFile(Element el, String name) {
	String s = el.getChildText(name);
	if ((s != null) && (getFile(s) == null)) {
	    log.debug("File " + s + " for element " + name + " in Element " + el.getAttributeValue("name") + " not found.");
	    return(false);
	}
	return(true);
    }

    protected boolean validateFiles(Element el, String name, String[] fnames) {
	List elist = el.getChildren(name);
	String s;
	if (elist.size() == 0) {
	    log.debug("No elements of name " + name);
	    return(false);
	}

	// Would like to get rid of this suppression, but I think it's fairly safe to assume a list of children
	// returned from an Element is going to be a list of Elements.
	@SuppressWarnings("unchecked")
	Iterator<Element> ns_i = elist.iterator();
	while(ns_i.hasNext()) {
	    Element ns_e = ns_i.next();
	    for (String fn : fnames) {
		s = ns_e.getChildText(fn);
		if ((s == null) || (getFile(s) == null)) {
		    log.debug("File " + s + " for element " + fn + " in Element " + ns_e.getAttributeValue("name") + " not found.");
		    return(false);
		}
	    }
	}
	// Made it this far, all is well.
	return(true);
    }
	// initialize logging
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDFile.class.getName());

}