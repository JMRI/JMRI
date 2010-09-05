// TrainManagerXml.java

package jmri.jmrit.operations.trains;

import java.io.File;
import java.util.List;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Loads and stores trains using xml files. Also stores various train
 * parameters managed by the TrainManager.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.20 $
 */
public class TrainManagerXml extends XmlFile {
	
	private boolean fileLoaded = false;
	
	public TrainManagerXml(){
		
	}
	
	/** record the single instance **/
	private static TrainManagerXml _instance = null;

	public static synchronized TrainManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("TrainManagerXml creating instance");
			// create and load
			_instance = new TrainManagerXml();
	           try {
	                _instance.readFile(defaultOperationsFilename());
	            } catch (Exception e) {
	                log.error("Exception during operations train file reading: "+e);
	            }
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("TrainManagerXml returns instance "+_instance);
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
	        Document doc = newDocument(root, dtdLocation+"operations-trains.dtd");

	        // add XSLT processing instruction
	        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
	        m.put("type", "text/xsl");
	        m.put("href", xsltLocation+"operations-trains.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);
	        
	        TrainManager manager = TrainManager.instance();
	        List<String> trainList = manager.getTrainsByIdList();
	        
	        for (int i=0; i<trainList.size(); i++){

	            //Extract the RosterEntry at this index and inspect the Comment and
	            //Decoder Comment fields to change any \n characters to <?p?> processor
	            //directives so they can be stored in the xml file and converted
	            //back when the file is read.
	        	String trainId = trainList.get(i);
	        	Train train = manager.getTrainById(trainId);
	            String tempComment = train.getComment();
	            StringBuffer buf = new StringBuffer();

	            //transfer tempComment to xmlComment one character at a time, except
	            //when \n is found.  In that case, insert <?p?>
	            for (int k = 0; k < tempComment.length(); k++) {
	                if (tempComment.startsWith("\n", k)) {
	                    buf.append("<?p?>");
	                }
	                else {
	                	buf.append(tempComment.substring(k, k + 1));
	                }
	            }
	            train.setComment(buf.toString());
	        }
	        //All Comments and Decoder Comment line feeds have been changed to processor directives


	        // add top-level elements      
	        root.addContent(manager.store());
	        Element values;
	        root.addContent(values = new Element("trains"));
	        // add entries
	        for (int i=0; i<trainList.size(); i++) {
	        	String trainId = trainList.get(i);
	        	Train train = manager.getTrainById(trainId);
 	            values.addContent(train.store());
	        }
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state for the
	        //Comment and Decoder comment fields, otherwise it can cause problems in
	        //other parts of the program (e.g. in copying a roster)
	        for (int i=0; i<trainList.size(); i++){
	        	String trainId = trainList.get(i);
	        	Train train = manager.getTrainById(trainId);
	            String xmlComment = train.getComment();
	            StringBuffer buf = new StringBuffer();

	            for (int k = 0; k < xmlComment.length(); k++) {
	                if (xmlComment.startsWith("<?p?>", k)) {
	                    buf.append("\n");
	                    k = k + 4;
	                }
	                else {
	                	buf.append(xmlComment.substring(k, k + 1));
	                }
	            }
	            train.setComment(buf.toString());
	        }

	        // done - train file now stored, so can't be dirty
	        setDirty(false);
	    }

	
	/**
     * Store the all of the operation train objects in the default place, including making a backup if needed
     */
    public void writeOperationsTrainFile() {
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
            log.error("Exception while writing the new operations file, may not be complete: "+e);
        }
    }
    
    /**
     * Read the contents of a roster XML file into this object. Note that this does not
     * clear any existing entries.
     */
    void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
    	
    	TrainManager manager = TrainManager.instance();
    	
    	// suppress rootFromName(name) warning message by checking to see if file exists
    	if (findFile(name) == null) {
    		log.debug(name + " file could not be found");
    		fileLoaded = true;	// set flag, could be the first time
    		return;
    	}
    	// find root
    	Element root = rootFromName(name);
    	if (root==null) {
    		log.debug(name + " file could not be read");
    		return;
    	}
    	if (log.isDebugEnabled()) XmlFile.dumpElement(root);

    	if (root.getChild("options") != null) {
    		Element e = root.getChild("options");
    		manager.options(e);
    	}

    	if (root.getChild("trains") != null) {
    		@SuppressWarnings("unchecked")
    		List<Element> l = root.getChild("trains").getChildren("train");
    		if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" trains");
    		for (int i=0; i<l.size(); i++) {
    			manager.register(new Train(l.get(i)));
    		}

    		fileLoaded = true;	// set flag

    		List<String> trainList = manager.getTrainsByIdList();

    		// load train icon if needed
    		for (int i = 0; i < trainList.size(); i++) {
    			//Get a RosterEntry object for this index
    			Train train = manager.getTrainById(trainList.get(i));
    			train.loadTrainIcon();
    		}

    		//Scan the object to check the Comment and Decoder Comment fields for
    		//any <?p?> processor directives and change them to back \n characters
    		for (int i = 0; i < trainList.size(); i++) {
    			//Get a RosterEntry object for this index
    			Train train = manager.getTrainById(trainList.get(i));

    			//Extract the Comment field and create a new string for output
    			String tempComment = train.getComment();
    			StringBuffer buf = new StringBuffer();

    			//transfer tempComment to xmlComment one character at a time, except
    			//when <?p?> is found.  In that case, insert a \n and skip over those
    			//characters in tempComment.
    			for (int k = 0; k < tempComment.length(); k++) {
    				if (tempComment.startsWith("<?p?>", k)) {
    					buf.append("\n");
    					k = k + 4;
    				}
    				else {
    					buf.append(tempComment.substring(k, k + 1));
    				}
    			}
    			train.setComment(buf.toString());
    		}
    	}
    	else {
    		log.error("Unrecognized operations train file contents in file: "+name);
    	}
    }
    
    public boolean isTrainFileLoaded(){
    	return fileLoaded;
    }

	/**
     * Store the train's build status
     */
    public File createTrainBuildReportFile(String name) {
    	if(backupFile)
    		makeBackupFile(defaultBuildReportFilename(name));
		File file = null;
		try {
			if (!checkFile(defaultBuildReportFilename(name))) {
				// The file does not exist, create it before writing
				file = new File(defaultBuildReportFilename(name));
				File parentDir = file.getParentFile();
				if (!parentDir.exists()){
					if (!parentDir.mkdir())
						log.error("Directory wasn't created");
				}
				if (file.createNewFile())
					log.debug("File created");
			} else {
				file = new File(defaultBuildReportFilename(name));
			}
		} catch (Exception e) {
			log.error("Exception while writing the new train build file, may not be complete: "
							+ e);
		}
		return file;
	}
    
    public File getTrainBuildReportFile(String name) {
    	File file = new File(defaultBuildReportFilename(name));
    	return file;
    }
     
    public String defaultBuildReportFilename(String name) { 
    	return XmlFile.prefsDir()+"operations"+File.separator+"buildstatus"+File.separator+BuildReportFileName+name+fileType;
    }
    public void setBuildReportName(String name) { BuildReportFileName = name; }
    private String BuildReportFileName = "train (";
    private String fileType =").txt";
    
    
	/**
     * Store the train's manifest
     */
    public File createTrainManifestFile(String name) {
    	if(backupFile)
    		makeBackupFile(defaultManifestFilename(name));
		File file = null;
		try {
			if (!checkFile(defaultManifestFilename(name))) {
				// The file does not exist, create it before writing
				file = new File(defaultManifestFilename(name));
				File parentDir = file.getParentFile();
				if (!parentDir.exists()){
					if (!parentDir.mkdir())
						log.error("Directory wasn't created");
				}
				if (file.createNewFile())
					log.debug("File created");
			} else {
				file = new File(defaultManifestFilename(name));
			}
		} catch (Exception e) {
			log.error("Exception while writing the new manifest file, may not be complete: "
							+ e);
		}
		return file;
	}
    
    public File getTrainManifestFile(String name) {
    	File file = new File(defaultManifestFilename(name));
    	return file;
    }
     
    public String defaultManifestFilename(String name) { 
    	return XmlFile.prefsDir()+"operations"+File.separator+"manifests"+File.separator+ManifestFileName+name+fileType;
    }
    public void setManifestName(String name) { ManifestFileName = name; }
    private String ManifestFileName = "train (";
 
    
	/**
     * Store the switch list for a location
     */
    public File createSwitchListFile(String name) {
    	if(backupFile)
    		makeBackupFile(defaultSwitchListName(name));
		File file = null;
		try {
			if (!checkFile(defaultSwitchListName(name))) {
				// The file does not exist, create it before writing
				file = new File(defaultSwitchListName(name));
				File parentDir = file.getParentFile();
				if (!parentDir.exists()){
					if (!parentDir.mkdir())
						log.error("Directory wasn't created");
				}
				if (file.createNewFile())
					log.debug("File created");
			} else {
				file = new File(defaultSwitchListName(name));
			}
		} catch (Exception e) {
			log.error("Exception while writing the new switchlist file, may not be complete: "
							+ e);
		}
		return file;
	}
    
    public File getSwitchListFile(String name) {
    	File file = new File(defaultSwitchListName(name));
    	return file;
    }
     
    public String defaultSwitchListName(String name) { 
    	return XmlFile.prefsDir()+"operations"+File.separator+"switchLists"+File.separator+SwitchListFileName+name+fileType;
    }
    public void setTrainSwitchListName(String name) { SwitchListFileName = name; }
    private String SwitchListFileName = "location (";
 
    private boolean dirty = false;
    void setDirty(boolean b) {dirty = b;}
    boolean isDirty() {return dirty;}
    
    private boolean backupFile = false;		// set to true to create backups during debug
   
    // Operation files always use the same directory
    public static String defaultOperationsFilename() { 
    	return OperationsXml.getFileLocation()+OperationsXml.getOperationsDirectoryName()+File.separator+getOperationsFileName();
    }

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    public static String getOperationsFileName(){
    	return OperationsFileName;
    }
    private static String OperationsFileName = "OperationsTrainRoster.xml";

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainManagerXml.class.getName());

}
