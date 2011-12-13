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
	    return(true);

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
	try {
	    return(getInputStream(this.getEntry(name)));
	} catch (IOException e) {
	    log.error("IOException caught " + e);
	    return(null);
	} catch(NullPointerException ne) {
	    log.error("Null Pointer Exception caught. name=" +name, ne);
	    return(null);
	}
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

	// initialize logging
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDFile.class.getName());

}