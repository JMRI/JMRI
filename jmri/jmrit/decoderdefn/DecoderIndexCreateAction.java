// DecoderIndexCreateAction.java

package jmri.jmrit.decoderdefn;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.io.*;
import javax.swing.*;
import org.jdom.*;
import org.jdom.input.*;
import com.sun.java.util.collections.List;

import jmri.jmrit.XmlFile;

/** 
 * Check the names in an XML decoder file against the names.xml definitions
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: DecoderIndexCreateAction.java,v 1.1 2002-02-28 21:47:08 jacobsen Exp $
 * @see             jmri.jmrit.XmlFile
 */
public class DecoderIndexCreateAction extends AbstractAction {
		
	public DecoderIndexCreateAction(String s) { 
		super(s);
	}
		
    public void actionPerformed(ActionEvent e) {
		// create an array of file names from prefs/decoders, count entries
		int i;
		int np = 0;
		String[] sp = null;
		XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+DecoderFile.fileLocation);
		File fp = new File(XmlFile.prefsDir()+DecoderFile.fileLocation);
		if (fp.exists()) {
			sp = fp.list();
			for (i=0; i<sp.length; i++) {
				if (sp[i].endsWith(".xml")) np++;
			}
		} else {
			log.warn(XmlFile.prefsDir()+"decoders was missing, though tried to create it");
		}
		// create an array of file names from xml/decoders, count entries
		String[] sx = (new File(XmlFile.xmlDir()+DecoderFile.fileLocation)).list();
		int nx = 0;
		for (i=0; i<sx.length; i++) {
			if (sx[i].endsWith(".xml")) nx++;
		}
		// copy the decoder entries to the final array
		// note: this results in duplicate entries if the same name is also local.
		// But for now I can live with that.
		String sbox[] = new String[np+nx];
		int n=0;
		if (sp != null && np> 0)
			for (i=0; i<sp.length; i++) {
				if (sp[i].endsWith(".xml")) sbox[n++] = sp[i];
			}
		for (i=0; i<sx.length; i++) {
			if (sx[i].endsWith(".xml")) sbox[n++] = sx[i];
		}
		
		// create a new decoderIndex
		DecoderIndexFile index = new DecoderIndexFile();
		
		// write it out
		try {
			index.writeFile("decoderIndex.xml", DecoderIndexFile.instance(), sbox);
		} catch (java.io.IOException ex) {
			log.error("Error writing new decoder index file: "+ex.getMessage());	
		}
	}		
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderIndexCreateAction.class.getName());
		
}
