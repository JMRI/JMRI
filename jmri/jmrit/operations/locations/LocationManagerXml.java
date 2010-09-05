// LocationManagerXml.java

package jmri.jmrit.operations.locations;

import java.io.File;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;


/**
 * Load and stores locations and schedules for operations.
 * 
 * @author Daniel Boudreau Copyright (C) 2008 2009
 * @version $Revision: 1.21 $
 */
public class LocationManagerXml extends XmlFile {
	
	public LocationManagerXml(){
		
	}
	
	/** record the single instance **/
	private static LocationManagerXml _instance = null;

	public static synchronized LocationManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("LocationManagerXml creating instance");
			// create and load
			_instance = new LocationManagerXml();
	           try {
	                _instance.readFile(defaultOperationsFilename());
	            } catch (Exception e) {
	                log.error("Exception during operations location file reading: "+e);
	            }
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("LocationManagerXml returns instance "+_instance);
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
	        Document doc = newDocument(root, dtdLocation+"operations-locations.dtd");

	        // add XSLT processing instruction
	        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
	        m.put("type", "text/xsl");
	        m.put("href", xsltLocation+"operations-locations.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);
	        
	        //Check the Comment and Decoder Comment fields for line breaks and
	        //convert them to a processor directive for storage in XML
	        //Note: this is also done in the LocoFile.java class to do
	        //the same thing in the individual locomotive roster files
	        //Note: these changes have to be undone after writing the file
	        //since the memory version of the roster is being changed to the
	        //file version for writing
	        LocationManager manager = LocationManager.instance();
	        List<String> locationList = manager.getLocationsByIdList();
	        
	        for (int i=0; i<locationList.size(); i++){

	            //Extract the RosterEntry at this index and inspect the Comment and
	            //Decoder Comment fields to change any \n characters to <?p?> processor
	            //directives so they can be stored in the xml file and converted
	            //back when the file is read.
	        	String locationId = locationList.get(i);
	        	Location loc = manager.getLocationById(locationId);
	            String tempComment = loc.getComment();
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
	            loc.setComment(buf.toString());
	        }
	        
	        // do the same for schedules
	        
	        ScheduleManager scheduleManager = ScheduleManager.instance();
	        List<String> scheduleList = scheduleManager.getSchedulesByIdList();
	        
	        for (int i=0; i<scheduleList.size(); i++){
	        	String scheduleId = scheduleList.get(i);
	        	Schedule sch = scheduleManager.getScheduleById(scheduleId);
	            String tempComment = sch.getComment();
	            StringBuffer buf = new StringBuffer();

	            for (int k = 0; k < tempComment.length(); k++) {
	                if (tempComment.startsWith("\n", k)) {
	                    buf.append("<?p?>");
	                }
	                else {
	                	buf.append(tempComment.substring(k, k + 1));
	                }
	            }
	            sch.setComment(buf.toString());
	        }
	        //All Comments line feeds have been changed to processor directives

	        // add top-level elements
	        root.addContent(manager.store());
	        Element values;
	        root.addContent(values = new Element("locations"));
	        // add entries
	        for (int i=0; i<locationList.size(); i++) {
	        	String locationId = locationList.get(i);
	        	Location loc = manager.getLocationById(locationId);
 	            values.addContent(loc.store());
	        }
	        
	        root.addContent(values = new Element("schedules"));
	        // add entries
	        for (int i=0; i<scheduleList.size(); i++) {
	        	String scheduleId = scheduleList.get(i);
	        	Schedule sch = scheduleManager.getScheduleById(scheduleId);
 	            values.addContent(sch.store());
	        }
	               
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state for the
	        //Comment and Decoder comment fields, otherwise it can cause problems in
	        //other parts of the program (e.g. in copying a roster)
	        for (int i=0; i<locationList.size(); i++){
	        	String locationId = locationList.get(i);
	        	Location loc = manager.getLocationById(locationId);
	            String xmlComment = loc.getComment();
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
	            loc.setComment(buf.toString());
	        }
	        
	        for (int i=0; i<scheduleList.size(); i++){
	        	String scheduleId = scheduleList.get(i);
	        	Schedule sch = scheduleManager.getScheduleById(scheduleId);
	            String xmlComment = sch.getComment();
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
	            sch.setComment(buf.toString());
	        }

	        // done - location file now stored, so can't be dirty
	        setDirty(false);
	    }

	
	/**
     * Store the all of the operation location objects in the default place, including making a backup if needed
     */
    public void writeOperationsLocationFile() {
    	LocationManagerXml.instance().makeBackupFile(defaultOperationsFilename());
        try {
          	 if(!LocationManagerXml.instance().checkFile(defaultOperationsFilename())){
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
        	LocationManagerXml.instance().writeFile(defaultOperationsFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new operations file, may not be complete: "+e);
        }
    }
    
    public void writeFileIfDirty(){
    	if(isDirty())
    		writeOperationsLocationFile();
    }
    
    /**
     * Read the contents of a roster XML file into this object. Note that this does not
     * clear any existing entries.
     */
    @SuppressWarnings("unchecked")
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
    	if (log.isDebugEnabled()) XmlFile.dumpElement(root);

    	LocationManager manager = LocationManager.instance();
    	if (root.getChild("options") != null) {
    		Element e = root.getChild("options");
    		manager.options(e);
    	}

    	// decode type, invoke proper processing routine if a decoder file
    	if (root.getChild("locations") != null) {

    		List<Element> l = root.getChild("locations").getChildren("location");
    		if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" locations");
    		for (int i=0; i<l.size(); i++) {
    			manager.register(new Location(l.get(i)));
    		}

    		List<String> locationList = manager.getLocationsByIdList();
    		//Scan the object to check the Comment and Decoder Comment fields for
    		//any <?p?> processor directives and change them to back \n characters
    		for (int i = 0; i < locationList.size(); i++) {
    			//Get a RosterEntry object for this index
    			String locationId = locationList.get(i);
    			Location loc = manager.getLocationById(locationId);

    			//Extract the Comment field and create a new string for output
    			String tempComment = loc.getComment();
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
    			loc.setComment(buf.toString());
    		}
    	}
    	else {
    		log.error("Unrecognized operations location file contents in file: "+name);
    	}

    	// now load schedules       
    	ScheduleManager scheduleManager = ScheduleManager.instance();

    	// decode type, invoke proper processing routine if a decoder file
    	if (root.getChild("schedules") != null) {

    		List<Element> l = root.getChild("schedules").getChildren("schedule");
    		if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" schedules");
    		for (int i=0; i<l.size(); i++) {
    			scheduleManager.register(new Schedule(l.get(i)));
    		}

    		List<String> scheduleList = scheduleManager.getSchedulesByIdList();
    		//Scan the object to check the Comment and Decoder Comment fields for
    		//any <?p?> processor directives and change them to back \n characters
    		for (int i = 0; i < scheduleList.size(); i++) {
    			//Get a RosterEntry object for this index
    			String scheduleId = scheduleList.get(i);
    			Schedule sch = scheduleManager.getScheduleById(scheduleId);

    			//Extract the Comment field and create a new string for output
    			String tempComment = sch.getComment();
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
    			sch.setComment(buf.toString());
    		}
    	}
    	else {
    		log.warn("Unrecognized operations location file contents in file: "+name);
    	}

    }

    private boolean dirty = false;
    void setDirty(boolean b) {dirty = b;}
    boolean isDirty() {return dirty;}


    // Operation files always use the same directory
    public static String defaultOperationsFilename() { 
    	return OperationsXml.getFileLocation()+OperationsXml.getOperationsDirectoryName()+File.separator+getOperationsFileName();
    }

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    public static String getOperationsFileName(){
    	return OperationsFileName;
    }
    private static String OperationsFileName = "OperationsLocationRoster.xml";

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocationManagerXml.class.getName());

}
