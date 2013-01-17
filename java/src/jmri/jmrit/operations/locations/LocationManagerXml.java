// LocationManagerXml.java

package jmri.jmrit.operations.locations;

import java.io.File;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.OperationsXml;

/**
 * Load and stores locations and schedules for operations.
 * 
 * @author Daniel Boudreau Copyright (C) 2008 2009 2010
 * @version $Revision$
 */
public class LocationManagerXml extends OperationsXml {

	public LocationManagerXml() {
	}

	/** record the single instance **/
	private static LocationManagerXml _instance = null;

	public static synchronized LocationManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("LocationManagerXml creating instance");
			// create and load
			_instance = new LocationManagerXml();
			_instance.load();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("LocationManagerXml returns instance " + _instance);
		return _instance;
	}

	public void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
		if (log.isDebugEnabled())
			log.debug("writeFile " + name);
		// This is taken in large part from "Java and XML" page 368
		File file = findFile(name);
		if (file == null) {
			file = new File(name);
		}
		// create root element
		Element root = new Element("operations-config"); // NOI18N
		Document doc = newDocument(root, dtdLocation + "operations-locations.dtd"); // NOI18N

		// add XSLT processing instruction
		java.util.Map<String, String> m = new java.util.HashMap<String, String>();
		m.put("type", "text/xsl"); // NOI18N
		m.put("href", xsltLocation + "operations-locations.xsl"); // NOI18N
		ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
		doc.addContent(0, p);
		
		LocationManager.instance().store(root);
		ScheduleManager.instance().store(root);

		writeXML(file, doc);

		// done - location file now stored, so can't be dirty
		setDirty(false);
	}

	/**
	 * Read the contents of a roster XML file into this object. Note that this does not clear any existing entries.
	 */
	public void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
		// suppress rootFromName(name) warning message by checking to see if file exists
		if (findFile(name) == null) {
			log.debug(name + " file could not be found");
			return;
		}
		// find root
		Element root = rootFromName(name);
		if (root == null) {
			log.debug(name + " file could not be read");
			return;
		}
		
		LocationManager.instance().load(root);
		ScheduleManager.instance().load(root);

		setDirty(false);
		log.debug("Locations have been loaded!");
	}

	public void setOperationsFileName(String name) {
		operationsFileName = name;
	}

	public String getOperationsFileName() {
		return operationsFileName;
	}

	private String operationsFileName = "OperationsLocationRoster.xml"; // NOI18N

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocationManagerXml.class
			.getName());

}
