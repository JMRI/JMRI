package jmri.jmrit.operations.setup;

import java.io.File;

import jmri.jmrit.XmlFile;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Loads and stores the operation setup using xml files. 
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.18 $
 */
public class OperationsXml extends XmlFile {
	
	public OperationsXml(){
		
	}
	
	/** record the single instance **/
	private static OperationsXml _instance = null;

    public synchronized static void resetInstance() { _instance = null; }

    public static synchronized OperationsXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("OperationsXml creating instance");
			// create and load
			_instance = new OperationsXml();
	           try {
	                _instance.readFile(defaultOperationsFilename());
	            } catch (Exception e) {
	                log.error("Exception during operations file reading",e);
	            }
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("OperationsXml returns instance "+_instance);
		return _instance;
	}
	

	void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
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

	        // add top-level elements
	        
	        root.addContent(Setup.store());

	        writeXML(file, doc);

	        // done, so can't be dirty
	        setDirty(false);
	    }
	
	void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
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
	}

	private boolean dirty = false;
	void setDirty(boolean b) {dirty = b;}
	boolean isDirty() {return dirty;}


	/**
	 * Store the all of the operation objects in the default place, including making a backup if needed
	 */
	public void writeOperationsFile() {
		makeBackupFile(defaultOperationsFilename());
		try {
			if(!checkFile(defaultOperationsFilename())){
				//The file does not exist, create it before writing
				java.io.File file=new java.io.File(defaultOperationsFilename());
				java.io.File parentDir=file.getParentFile();
                if (!parentDir.exists()){
                    if (!parentDir.mkdir())
                    	log.error("Directory wasn't created");
                 }
                 if (file.createNewFile())
                	 log.debug("File created");
			}
			writeFile(defaultOperationsFilename());
		} catch (Exception e) {
			log.error("Exception while writing the new operations file, may not be complete",e);
		}
	}

	public static String defaultOperationsFilename() { return getFileLocation()+getOperationsDirectoryName()+File.separator+getOperationsFileName();}

	public static void setOperationsDirectoryName(String name) { operationsDirectoryName = name; }
	public static String getOperationsDirectoryName(){
		return operationsDirectoryName;
	}
	private static String operationsDirectoryName = "operations";

	public static void setOperationsFileName(String name) { operationsFileName = name; }
	public static String getOperationsFileName(){
		return operationsFileName;
	}
	private static String operationsFileName = "Operations.xml";

    /**
     * Set the default location for Operations files.
     *
     * @param f Absolute pathname to use. A null or "" argument flags
     * a return to the original default in prefsdir.
     */
    public static void setFileLocation(String f) {
        if (f!=null && !f.equals("")) {
            if (f.endsWith(File.separator))
                fileLocation = f;
            else
                fileLocation = f+File.separator;
        } else {
            if (log.isDebugEnabled()) log.debug("Operations location reset to default");
            fileLocation = XmlFile.prefsDir();
        }
        // and make sure next request gets the new one
        resetInstance();
    }

    /**
     * Absolute path to location of Operations files.
     * <P>
     * Default is in the prefsDir, but can be set to anything.
     * @see XmlFile#prefsDir()
     */
    public static String getFileLocation() { return fileLocation; }
    private static String fileLocation = XmlFile.prefsDir();

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OperationsXml.class.getName());

}
