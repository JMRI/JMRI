// TrainManagerXml.java

package jmri.jmrit.operations.trains;

import java.io.File;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import jmri.jmrit.XmlFile;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.roster.Roster;

public class TrainManagerXml extends XmlFile {
	
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
	

	void writeFile(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException, java.io.IOException {
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
	        java.util.Map m = new java.util.HashMap();
	        m.put("type", "text/xsl");
	        m.put("href", xsltLocation+"operations-trains.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);
	        
	        //Check the Comment and Decoder Comment fields for line breaks and
	        //convert them to a processor directive for storage in XML
	        //Note: this is also done in the LocoFile.java class to do
	        //the same thing in the indidvidual locomotive roster files
	        //Note: these changes have to be undone after writing the file
	        //since the memory version of the roster is being changed to the
	        //file version for writing
	        TrainManager manager = TrainManager.instance();
	        List trainList = manager.getTrainsByIdList();
	        
	        for (int i=0; i<trainList.size(); i++){

	            //Extract the RosterEntry at this index and inspect the Comment and
	            //Decoder Comment fields to change any \n characters to <?p?> processor
	            //directives so they can be stored in the xml file and converted
	            //back when the file is read.
	        	String trainId = (String)trainList.get(i);
	        	Train train = manager.getTrainById(trainId);
	            String tempComment = train.getComment();
	            String xmlComment = new String();

	            //transfer tempComment to xmlComment one character at a time, except
	            //when \n is found.  In that case, insert <?p?>
	            for (int k = 0; k < tempComment.length(); k++) {
	                if (tempComment.startsWith("\n", k)) {
	                    xmlComment = xmlComment + "<?p?>";
	                }
	                else {
	                    xmlComment = xmlComment + tempComment.substring(k, k + 1);
	                }
	            }
	            train.setComment(xmlComment);
	        }
	        //All Comments and Decoder Comment line feeds have been changed to processor directives


	        // add top-level elements
	        Element values;
	        
	        root.addContent(values = new Element("options"));
	        values.addContent(manager.store());

	        root.addContent(values = new Element("trains"));
	        // add entries
	        for (int i=0; i<trainList.size(); i++) {
	        	String trainId = (String)trainList.get(i);
	        	Train train = manager.getTrainById(trainId);
 	            values.addContent(train.store());
	        }
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state for the
	        //Comment and Decoder comment fields, otherwise it can cause problems in
	        //other parts of the program (e.g. in copying a roster)
	        for (int i=0; i<trainList.size(); i++){
	        	String trainId = (String)trainList.get(i);
	        	Train train = manager.getTrainById(trainId);
	            String xmlComment = train.getComment();
	            String tempComment = new String();

	            for (int k = 0; k < xmlComment.length(); k++) {
	                if (xmlComment.startsWith("<?p?>", k)) {
	                    tempComment = tempComment + "\n";
	                    k = k + 4;
	                }
	                else {
	                    tempComment = tempComment + xmlComment.substring(k, k + 1);
	                }
	            }
	            train.setComment(tempComment);
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
          	 if(!checkFile(defaultOperationsFilename()))
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
        // find root
        Element root = rootFromName(name);
        if (root==null) {
            log.debug(name + " file could not be read");
            return;
        }
        if (log.isDebugEnabled()) XmlFile.dumpElement(root);
        
        TrainManager manager = TrainManager.instance();

        if (root.getChild("options") != null) {
        	Element e = root.getChild("options").getChild("trainOptions");
        	manager.options(e);
        }
        
        if (root.getChild("trains") != null) {
            List l = root.getChild("trains").getChildren("train");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" trains");
            for (int i=0; i<l.size(); i++) {
                manager.register(new Train((Element)l.get(i)));
            }

            List trainList = manager.getTrainsByIdList();
            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < trainList.size(); i++) {
                //Get a RosterEntry object for this index
            	String trainId = (String)trainList.get(i);
	        	Train train = manager.getTrainById(trainId);

                //Extract the Comment field and create a new string for output
                String tempComment = train.getComment();
                String xmlComment = new String();

                //transfer tempComment to xmlComment one character at a time, except
                //when <?p?> is found.  In that case, insert a \n and skip over those
                //characters in tempComment.
                for (int k = 0; k < tempComment.length(); k++) {
                    if (tempComment.startsWith("<?p?>", k)) {
                        xmlComment = xmlComment + "\n";
                        k = k + 4;
                    }
                    else {
                        xmlComment = xmlComment + tempComment.substring(k, k + 1);
                    }
                }
                train.setComment(xmlComment);
            }
        }
        else {
            log.error("Unrecognized operations train file contents in file: "+name);
        }
    }
    
	/**
     * Store the train's build status
     */
    public File createTrainBuildReportFile(String name) {
		makeBackupFile(defaultBuildReportFilename(name));
		File file = null;
		try {
			if (!checkFile(defaultBuildReportFilename(name))) {
				// The file does not exist, create it before writing
				file = new File(defaultBuildReportFilename(name));
				File parentDir = file.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdir();
				}
				file.createNewFile();
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
		makeBackupFile(defaultManifestFilename(name));
		File file = null;
		try {
			if (!checkFile(defaultManifestFilename(name))) {
				// The file does not exist, create it before writing
				file = new File(defaultManifestFilename(name));
				File parentDir = file.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdir();
				}
				file.createNewFile();
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
     * Store the switchlist for a location
     */
    public File createSwitchListFile(String name) {
		makeBackupFile(defaultManifestFilename(name));
		File file = null;
		try {
			if (!checkFile(defaultManifestFilename(name))) {
				// The file does not exist, create it before writing
				file = new File(defaultSwitchListName(name));
				File parentDir = file.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdir();
				}
				file.createNewFile();
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

    
    public static String defaultOperationsFilename() { return XmlFile.prefsDir()+"operations"+File.separator+OperationsFileName;}

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    private static String OperationsFileName = "OperationsTrainRoster.xml";

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrainManagerXml.class.getName());

}
