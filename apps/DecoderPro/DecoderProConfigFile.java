// DecoderProConfigFile.java

package jmri.apps;

import com.sun.java.util.collections.List;
import java.io.*;
import java.util.Date;
import jmri.jmrit.XmlFile;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * Represents and manipulates the preferences information for the
 * DecoderPro application. Works with the DecoderProConfigFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		 	$Id: DecoderProConfigFile.java,v 1.2 2001-12-05 23:31:15 jacobsen Exp $
 * @see jmri.apps.DecodeProConfigFrame
 */
public class DecoderProConfigFile extends XmlFile {
	
	public void readFile(String name) throws java.io.FileNotFoundException, org.jdom.JDOMException {
		Element root = rootFromFile(name);
		_connection = root.getChild("connection");
		_gui = root.getChild("gui");
		_programmer = root.getChild("programmer");
	}
	
	// access to the three elements
	public Element getConnectionElement() {
		return _connection;
	}
	
	public Element getGuiElement() {
		return _gui;
	}
	
	public Element getProgrammerElement() {
		return _programmer;
	}
	
	Element _connection;
	Element _gui;
	Element _programmer;

	public void writeFile(String name, DecoderProConfigFrame f) {
		try {
			// This is taken in large part from "Java and XML" page 368 

			// create file Object
			File file = new File(name);
			
			// create root element
			Element root = new Element("DecoderPro-config");
			Document doc = new Document(root);
			doc.setDocType(new DocType("DecoderPro-config","DecoderPro-config.dtd"));
		
			// add connection element
			Element values;
			root.addContent(f.getConnection());
					
			// add gui element
			root.addContent(f.getGUI())
				;

			// add programmer element
			root.addContent(new Element("programmer")
					.addAttribute("defaultFile", "MultiPane.xml")
					.addAttribute("verifyBeforeWrite", "no")
					)
				;
			
			// write the result to selected file
			java.io.FileOutputStream o = new java.io.FileOutputStream(file);
			XMLOutputter fmt = new XMLOutputter();
			fmt.setNewlines(true);   // pretty printing
			fmt.setIndent(true);
			fmt.output(doc, o);
			
			}
		catch (Exception e) {
			log.error(e);
		}
	}

	/** 
	* Move original file to a backup. Use this before writing out a new version of the file
	*/
	protected void makeBackupFile() {
		File file = new File(defaultConfigFilename());
		if (file.exists()) {
			file.renameTo(backupFileName(configFileName));
		}
		else log.warn("No DecoderPro configuration file to backup");
	}

	/** 
	* Return a File reference to a new, unique backup file. This is here so it can 
	* be overridden during tests.
	*/
	static public File backupFileName(String name) {
		// File.createTempFile is not available in java 1, so use millisecond time as unique string
		File f =  new File(fileLocation+File.separator+name+"-"
							+((new Date()).getTime()));
		if (log.isDebugEnabled()) log.debug("backup file name is "+f.getAbsolutePath());
		return f;
	}

	static protected String fileLocation  = "prefs";
	static protected String configFileName = "DecoderProConfig.xml";

	public static String defaultConfigFilename() { return fileLocation+File.separator+configFileName;}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProConfigFile.class.getName());
		
}
