// AbstractConfigFile.java

package jmri.apps;

import com.sun.java.util.collections.List;
import java.io.*;
import jmri.jmrit.XmlFile;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * Abstract base class to represent and manipulate the preferences information for an
 * application. Works with the AbstractConfigFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		 	$Id: AbstractConfigFile.java,v 1.1 2002-02-20 07:33:45 jacobsen Exp $
 * @see jmri.apps.AbstractConfigFrame
 */
abstract public class AbstractConfigFile extends XmlFile {
	
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

	abstract public void writeFile(String name, AbstractConfigFrame f);

	abstract protected String configFileName();

	public String defaultConfigFilename() { return configFileName();}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractConfigFile.class.getName());
		
}
