// XmlFile.java

package jmri.jmrit;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.util.Date;

import java.io.*;

import org.jdom.*;
import org.jdom.input.*;
import com.sun.java.util.collections.*;

/** 
 * XmlFile contains various member implementations for handling aspects of XML files.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @version		$Id: XmlFile.java,v 1.6 2002-01-01 01:57:25 jacobsen Exp $	
 */
public abstract class XmlFile {
	
	
	/**
	 * Read the contents of an XML file from its name.  The search order is implemented in 
	 * this routine via testing for the existance, not the parsebility, of the files
	 */
	public Element rootFromName(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		
		File fp = findFile(name);
		if (fp != null) {
			if (log.isDebugEnabled()) log.debug("readFile: "+name+" from "+fp.getAbsolutePath()); 
			return rootFromFile(fp);
		}
		else {
			log.warn("Did not find file "+name+" in "+prefsDir()+" or "+xmlDir());
			return null;
		}
	}

	/**
	 * Read a File as XML
	 */
	public Element rootFromFile(File file) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		return rootFromStream(new BufferedInputStream(new FileInputStream(file)));
	}

	/**
	 * Read the contents of a stream as XML, and return the root object
	 */
	public Element rootFromStream(InputStream stream) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		String rawpath = new File("xml"+File.separator+"DTD"+File.separator).getAbsolutePath();
		String apath = rawpath;
        if (File.separatorChar != '/') {
            apath = apath.replace(File.separatorChar, '/');
        }
        if (!apath.startsWith("/")) {
            apath = "/" + apath;
        }
        if (!apath.endsWith("/")) {
            apath = apath+"/";
        }
		String path = "file:"+apath;

		if (log.isDebugEnabled()) log.debug("readFile from stream, search path:"+path); 
		// This is taken in large part from "Java and XML" page 354
			
		// Open and parse file
		
		// DOMBuilder builder = new DOMBuilder(verify);  // argument controls validation, on for now
		// Document doc = builder.build(new BufferedInputStream(stream));
		
		SAXBuilder builder = new SAXBuilder(verify);  // argument controls validation, on for now
		Document doc = builder.build(new BufferedInputStream(stream),path);
		
		// find root
		return doc.getRootElement();			
	}

	
	/** 
	* Check if a file of the given name exists. This is here so it can 
	* be overridden during tests. Note that it also obeys the
	* search rules. 
	* @parameter name subdirectory and file name, not including the leading xml or prefs
	*/
	protected boolean checkFile(String name) { 
		File fp = new File(prefsDir()+name);
		if (fp.exists()) {
			return true;
		}
		else {
			File fx = new File(xmlDir()+name);
			if (fx.exists()) {
				return true;
			}
			else {
			return false;
			}
		}
	}


	/** 
	* Return a File object for a name. This is here to implement the
	* search rule: Look first in prefsDir, then xmlDir()
	*/
	protected File findFile(String name) { 
		File fp = new File(prefsDir()+name);
		if (fp.exists()) {
			return fp;
		}
		else {
			File fx = new File(xmlDir()+name);
			if (fx.exists()) {
				return fx;
			}
			else {
			return null;
			}
		}
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
	
	/** 
	* Move original file to a backup. Use this before writing out a new version of the file.
	* The argument is subdir/name, without the xml/ or prefs/
	*/
	public void makeBackupFile(String name) {
		File file = findFile(name);
		if (file!=null) {
			file.renameTo(new File(backupFileName(name)));
		}
		else log.info("No "+name+" file to backup");
	}

	/** 
	* Return the name of a new, unique backup file. This is here so it can 
	* be overridden during tests.
	*/
	public String backupFileName(String name) {
		// File.createTempFile is not available in java 1, so use millisecond time as unique string
		String f = prefsDir()+name+"-"
							+((new Date()).getTime());
		if (log.isDebugEnabled()) log.debug("backup file name is "+f);
		return f;
	}

	static public String xmlDir() {return "xml"+File.separator;}
	static public String prefsDir() {return "prefs"+File.separator;}
	
	static boolean verify = true;
	
	// initialize SAXbuilder
	static private SAXBuilder builder = new SAXBuilder(verify);  // argument controls validation, on for now
	
	// initialize logging	
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlFile.class.getName());
		
}
