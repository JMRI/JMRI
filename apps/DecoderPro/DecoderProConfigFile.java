// DecoderProConfigFile.java

package jmri.apps;

import com.sun.java.util.collections.List;
import java.io.*;
import jmri.jmrit.XmlFile;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * Represents and manipulates the preferences information for the
 * DecoderPro application. Works with the DecoderProConfigFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		 	$Id: DecoderProConfigFile.java,v 1.3 2001-12-18 07:21:33 jacobsen Exp $
 * @see jmri.apps.DecodeProConfigFrame
 */
public class DecoderProConfigFile extends XmlFile {
	
	public void readFile(String name) throws java.io.FileNotFoundException, org.jdom.JDOMException {
		Element root = rootFromName(name);
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
			File file = new File(prefsDir()+name);
			
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

	static protected String configFileName = "DecoderProConfig.xml";

	public static String defaultConfigFilename() { return configFileName;}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProConfigFile.class.getName());
		
}
