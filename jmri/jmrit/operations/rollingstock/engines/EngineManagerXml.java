// CarManagerXml.java

package jmri.jmrit.operations.rollingstock.engines;

import java.io.File;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import jmri.jmrit.XmlFile;
//import jmri.jmrit.operations.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.setup.OperationsXml;

import jmri.jmrit.roster.Roster;

public class EngineManagerXml extends XmlFile {
	
	public EngineManagerXml(){
		
	}
	
	/** record the single instance **/
	private static EngineManagerXml _instance = null;

	public static synchronized EngineManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineManagerXml creating instance");
			// create and load
			_instance = new EngineManagerXml();
	           try {
	                _instance.readFile(defaultOperationsFilename());
	            } catch (Exception e) {
	                log.error("Exception during operations car file reading: "+e);
	            }
		}
		if (log.isDebugEnabled()) log.debug("EngineManagerXml returns instance "+_instance);
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
	        Document doc = newDocument(root, dtdLocation+"operations-engines.dtd");

	        // add XSLT processing instruction
	        java.util.Map m = new java.util.HashMap();
	        m.put("type", "text/xsl");
	        m.put("href", xsltLocation+"operations-engines.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);
	        
	        //Check the Comment and Decoder Comment fields for line breaks and
	        //convert them to a processor directive for storage in XML
	        //Note: this is also done in the LocoFile.java class to do
	        //the same thing in the indidvidual locomotive roster files
	        //Note: these changes have to be undone after writing the file
	        //since the memory version of the roster is being changed to the
	        //file version for writing
	        EngineManager manager = EngineManager.instance();
	        List engineList = manager.getEnginesByRoadNameList();
	        
	        for (int i=0; i<engineList.size(); i++){

	            //Extract the RosterEntry at this index and inspect the Comment and
	            //Decoder Comment fields to change any \n characters to <?p?> processor
	            //directives so they can be stored in the xml file and converted
	            //back when the file is read.
	        	String engineId = (String)engineList.get(i);
	        	Engine c = manager.getEngineById(engineId);
	            String tempComment = c.getComment();
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
	            c.setComment(xmlComment);
	        }
	        //All Comments and Decoder Comment line feeds have been changed to processor directives


	        // add top-level elements
	        Element values;
	        root.addContent(values = new Element("engineModels"));
	        String[]types = EngineModels.instance().getNames();
	        for (int i=0; i<types.length; i++){
	        	String typeNames = types[i]+"%%";
	        	values.addContent(typeNames);
	        }
	        root.addContent(values = new Element("engineLengths"));
	        String[]lengths = EngineLengths.instance().getNames();
	        for (int i=0; i<lengths.length; i++){
	        	String lengthNames = lengths[i]+"%%";
	        	values.addContent(lengthNames);
	        }
	        root.addContent(values = new Element("consists"));
	        List consists = manager.getConsistNameList();
	        for (int i=0; i<consists.size(); i++){
	        	String consistNames = (String)consists.get(i)+"%%";
	        	values.addContent(consistNames);
	        }
	        root.addContent(values = new Element("engines"));
	        // add entries
	        for (int i=0; i<engineList.size(); i++) {
	        	String engineId = (String)engineList.get(i);
	        	Engine c = manager.getEngineById(engineId);
	            values.addContent(c.store());
	        }
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state for the
	        //Comment and Decoder comment fields, otherwise it can cause problems in
	        //other parts of the program (e.g. in copying a roster)
	        for (int i=0; i<engineList.size(); i++){
	        	String engineId = (String)engineList.get(i);
	        	Engine c = manager.getEngineById(engineId);
	            String xmlComment = c.getComment();
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
	            c.setComment(tempComment);
	        }

	        // done - engine file now stored, so can't be dirty
	        setDirty(false);
	    }

	
	/**
     * Store the all of the operation engine objects in the default place, including making a backup if needed
     */
    public void writeOperationsEngineFile() {
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
        
        EngineManager manager = EngineManager.instance();
        
        if (root.getChild("engineModels")!= null){
        	String names = root.getChildText("engineModels");
        	String[] types = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("engine models: "+names);
        	EngineModels.instance().setNames(types);
        }
          
        if (root.getChild("engineLengths")!= null){
        	String names = root.getChildText("engineLengths");
        	String[] lengths = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("engine lengths: "+names);
        	EngineLengths.instance().setNames(lengths);
        }
        
        if (root.getChild("consists")!= null){
        	String names = root.getChildText("consists");
        	if(!names.equals("")){
        		String[] consistNames = names.split("%%");
        		if (log.isDebugEnabled()) log.debug("consists: "+names);
        		for (int i=0; i<consistNames.length; i++){
        			manager.newConsist(consistNames[i]);
        		}
        	}
        }
         
        if (root.getChild("engines") != null) {
        	
            List l = root.getChild("engines").getChildren("engine");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" engines");
            for (int i=0; i<l.size(); i++) {
                manager.register(new Engine((Element)l.get(i)));
            }

            List engineList = manager.getEnginesByRoadNameList();
            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < engineList.size(); i++) {
                //Get a RosterEntry object for this index
            	String engineId = (String)engineList.get(i);
	        	Engine c = manager.getEngineById(engineId);

                //Extract the Comment field and create a new string for output
                String tempComment = c.getComment();
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
                c.setComment(xmlComment);
            }
        }
        else {
            log.error("Unrecognized operations engine file contents in file: "+name);
        }
    }

    private boolean dirty = false;
    void setDirty(boolean b) {dirty = b;}
    boolean isDirty() {return dirty;}

    
    public static String defaultOperationsFilename() { return XmlFile.prefsDir()+"operations"+File.separator+OperationsFileName;}

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    private static String OperationsFileName = "OperationsEngineRoster.xml";

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EngineManagerXml.class.getName());

}
