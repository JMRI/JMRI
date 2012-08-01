package jmri.jmrit.operations.setup;

import java.io.File;

import jmri.jmrit.operations.OperationsXml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Loads and stores the operation setup using xml files. 
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */
public class OperationsSetupXml extends OperationsXml {
	
	public OperationsSetupXml(){
	}
	
	/** record the single instance **/
	private static OperationsSetupXml _instance = null;

    public static synchronized OperationsSetupXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("OperationsSetupXml creating instance");
			// create and load
			_instance = new OperationsSetupXml();
			_instance.load();
			
			// Do an AutoBackup after we have read in the SetupXml to know if
			// they are enabled or not.
			if (Setup.isAutoBackupEnabled()) {
				try {
					AutoBackup backup = new AutoBackup();
					backup.autoBackup();
				} catch (Exception ex) {
					log.debug("Auto backup after enabling Auto Backup flag.", ex);
				}
			}

		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("OperationsSetupXml returns instance "+_instance);
		return _instance;
	}
	
	public void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
	        if (log.isDebugEnabled()) log.debug("writeFile "+name);
	        // This is taken in large part from "Java and XML" page 368
	        File file = findFile(name);
	        if (file == null) {
	            file = new File(name);
	        }
	        // create root element
	        Element root = new Element("operations-config");
	        Document doc = newDocument(root, dtdLocation+"operations-config.dtd");

	        // add XSLT processing instruction
	        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
	        m.put("type", "text/xsl");
	        m.put("href", xsltLocation+"operations-config.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);

	        Setup.setMiaComment(convertToXmlComment(Setup.getMiaComment()));	        
	        // add top-level elements	        
	        root.addContent(Setup.store());
	        // add control elements
	        root.addContent(Control.store());

	        writeXML(file, doc);	        
	        Setup.setMiaComment(convertFromXmlComment(Setup.getMiaComment()));

	        // done, so can't be dirty
	        setDirty(false);
	    }
	
	public void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
		// suppress rootFromName(name) warning message by checking to see if file exists
		if (findFile(name) == null) {
			log.debug(name + " file could not be found");
			return;
		}
		// find root
		Element root = rootFromName(name);
		if (root==null) {
			log.debug(name + " file could not be read");
			return;
		}
		Setup.load(root);		
        Setup.setMiaComment(convertFromXmlComment(Setup.getMiaComment()));
        // load control settings
        Control.load(root);
	}
	
	public void setOperationsFileName(String name) { operationsFileName = name; }
	public String getOperationsFileName(){
		return operationsFileName;
	}

	private String operationsFileName = "Operations.xml";

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OperationsSetupXml.class.getName());

}
