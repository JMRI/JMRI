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
import com.sun.java.util.collections.*;

/** 
 * XmlFile contains various member implementations for handling aspects of XML files.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @version		$Id: XmlFile.java,v 1.3 2001-11-27 03:27:15 jacobsen Exp $	
 */
public abstract class XmlFile {
	
	/**
	 * Read the contents of an XML file, using the abstract service routines.
	 */
	public Element rootFromFile(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		String rawpath = new File("xml"+File.separator+"DTD"+File.separator).getAbsolutePath();
		String apath = rawpath;
        if (File.separatorChar != '/') {
            apath = apath.replace(File.separatorChar, '/');
        }
        if (!apath.startsWith("/")) {
            apath = "/" + apath;
        }
		String path = "file::"+apath;

		if (log.isInfoEnabled()) log.info("readFile: "+name+" path: "+rawpath); 
		// This is taken in large part from "Java and XML" page 354
			
		// Open and parse file
		
		// DOMBuilder builder = new DOMBuilder(verify);  // argument controls validation, on for now
		// Document doc = builder.build(new BufferedInputStream(new FileInputStream(new File(name))));
		
		SAXBuilder builder = new SAXBuilder(verify);  // argument controls validation, on for now
		Document doc = builder.build(new BufferedInputStream(new FileInputStream(new File(name))),rawpath+File.separator);
		
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


	/** 
	* Diagnostic printout of as much as we can find
	*/
	static public void dumpElement(Element name) { 
		List l = name.getChildren();
		for (int i = 0; i<l.size(); i++) {
			System.out.println(" Element: "+((Element)l.get(i)).getName()+" ns: "+((Element)l.get(i)).getNamespace());
		}
	}
	
	static boolean verify = true;
	
	// initialize SAXbuilder
	static private SAXBuilder builder = new SAXBuilder(verify);  // argument controls validation, on for now
	
	// initialize logging	
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlFile.class.getName());
		
}
