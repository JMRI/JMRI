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

import java.util.zip.*;
import java.io.*;

class VSDFile extends ZipFile {

    ZipInputStream zis;

    public VSDFile(File file) throws ZipException, IOException {
	super(file);
    }

    public VSDFile(File file, int mode) throws ZipException, IOException {
	super(file, mode);
    }

    public VSDFile(String name) throws ZipException, IOException {
	super(name);
    }

    public java.io.InputStream getInputStream(String name) {
	try {
	    return(getInputStream(this.getEntry(name)));
	} catch (IOException e) {
	    log.warn("IOException caught " + e);
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