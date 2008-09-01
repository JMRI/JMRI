package jmri.jmrit.operations.setup;

import java.io.File;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.Setup;

import jmri.jmrit.roster.Roster;

public class OperationsXml extends XmlFile {
	
	public OperationsXml(){
		
	}
	
	/** record the single instance **/
	private static OperationsXml _instance = null;

	public static synchronized OperationsXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("OperationsXml creating instance");
			// create and load
			_instance = new OperationsXml();
	           try {
	                _instance.readFile(defaultOperationsFilename());
	            } catch (Exception e) {
	                log.error("Exception during operations file reading: "+e);
	            }
		}
		if (log.isDebugEnabled()) log.debug("OperationsXml returns instance "+_instance);
		return _instance;
	}
	

	void writeFile(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException, java.io.IOException {
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
	        java.util.Map m = new java.util.HashMap();
	        m.put("type", "text/xsl");
	        m.put("href", "http://jmri.sourceforge.net/xml/XSLT/operations.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);

	        // add top-level elements
	        
	        root.addContent(Setup.store());

	        writeXML(file, doc);

	        // done, so can't be dirty
	        setDirty(false);
	    }
	
	   void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
	        // find root
	        Element root = rootFromName(name);
	        if (root==null) {
	            log.debug(name + " file could not be read");
	            return;
	        }
	        Setup.load(root);
	    }

	    private boolean dirty = false;
	    void setDirty(boolean b) {dirty = b;}
	    boolean isDirty() {return dirty;}

	
	/**
     * Store the all of the operation objects in the default place, including making a backup if needed
     */
    public static void writeOperationsFile() {
    	OperationsXml.instance().makeBackupFile(defaultOperationsFilename());
        try {
        	 if(!OperationsXml.instance().checkFile(defaultOperationsFilename()))
             {
                 //The file does not exist, create it before writing
                 java.io.File file=new java.io.File(defaultOperationsFilename());
                 java.io.File parentDir=file.getParentFile();
                 if(!parentDir.exists())
                 {
                    parentDir.mkdir();
                 }
                 file.createNewFile();
             }
        	OperationsXml.instance().writeFile(defaultOperationsFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new operations file, may not be complete: "+e);
        }
    }
    
    public static String defaultOperationsFilename() { return XmlFile.prefsDir()+"operations"+File.separator+OperationsFileName;}

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    private static String OperationsFileName = "Operations.xml";

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(OperationsXml.class.getName());

}
