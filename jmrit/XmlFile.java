// XmlFile.java

package jmri.jmrit;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.io.*;

import org.jdom.*;
import org.jdom.input.*;

/** 
 * XmlFile contains various member implementations for handling aspects of XML files.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @version		$Id: XmlFile.java,v 1.1 2001-11-11 06:56:43 jacobsen Exp $	
 */
public abstract class XmlFile {
	
	abstract public Namespace getNamespace();

	/**
	 * Read the contents of an XML file, using the abstract service routines.
	 */
	protected Element rootFromFile(String name, boolean verify) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		if (log.isInfoEnabled()) log.info("readFile "+name);
		// This is taken in large part from "Java and XML" page 354
			
		// Open and parse file
		Namespace ns = getNamespace();
		
		SAXBuilder builder = new SAXBuilder(verify);  // argument controls validation, on for now
		Document doc = builder.build(new FileInputStream(new File(name)),"xml"+File.separator);
		// find root
		return doc.getRootElement();
			
	}

	/** 
	* Check if a file of the given name exists. This is here so it can 
	* be overridden during tests.
	*/
	protected boolean checkFile(String name) { 
		File file = new File(name);
		return file.exists();
	}

	// initialize logging	
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlFile.class.getName());
		
}
