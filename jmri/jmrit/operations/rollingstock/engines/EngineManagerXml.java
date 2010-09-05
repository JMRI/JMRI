// CarManagerXml.java

package jmri.jmrit.operations.rollingstock.engines;

import java.io.File;
import java.util.List;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Loads and stores engines using xml files. Also loads and stores engine
 * models, engine types, engine lengths, and engine consist names.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.21 $
 */
public class EngineManagerXml extends XmlFile {
	
	public EngineManagerXml(){
		
	}
	
	/** record the single instance **/
	static protected EngineManagerXml _instance = null;

	public static synchronized EngineManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineManagerXml creating instance");
			// create and load
			_instance = new EngineManagerXml();
	           try {
	                _instance.readFile(defaultOperationsFilename());
	            } catch (Exception e) {
	                log.error("Exception during operations engine file reading: "+e);
	            }
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("EngineManagerXml returns instance "+_instance);
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
	        Document doc = newDocument(root, dtdLocation+"operations-engines.dtd");

	        // add XSLT processing instruction
	        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
	        m.put("type", "text/xsl");
	        m.put("href", xsltLocation+"operations-engines.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);
	        
	        //Check the Comment and Decoder Comment fields for line breaks and
	        //convert them to a processor directive for storage in XML
	        EngineManager manager = EngineManager.instance();
	        List<String> engineList = manager.getByRoadNameList();
	        
	        for (int i=0; i<engineList.size(); i++){
	            //Extract the RosterEntry at this index and inspect the Comment and
	            //Decoder Comment fields to change any \n characters to <?p?> processor
	            //directives so they can be stored in the xml file and converted
	            //back when the file is read.
	        	Engine e = manager.getById(engineList.get(i));
	            String tempComment = e.getComment();
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
	            e.setComment(buf.toString());
	        }
	        //All Comments and Decoder Comment line feeds have been changed to processor directives


	        // add top-level elements
	        root.addContent(manager.store());
	        Element values;
	        root.addContent(values = new Element("engineModels"));
	        String[]models = EngineModels.instance().getNames();
	        for (int i=0; i<models.length; i++){
	        	String typeNames = models[i]+"%%";
	        	values.addContent(typeNames);
	        }
	        root.addContent(values = new Element("engineTypes"));
	        String[]types = EngineTypes.instance().getNames();
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
	        List<String> consists = manager.getConsistNameList();
	        for (int i=0; i<consists.size(); i++){
	        	String consistNames = consists.get(i)+"%%";
	        	values.addContent(consistNames);
	        }
	        root.addContent(values = new Element("engines"));
	        // add entries
	        for (int i=0; i<engineList.size(); i++) {
	        	String engineId = engineList.get(i);
	        	Engine c = manager.getById(engineId);
	            values.addContent(c.store());
	        }
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state for the
	        //Comment and Decoder comment fields, otherwise it can cause problems in
	        //other parts of the program (e.g. in copying a roster)
	        for (int i=0; i<engineList.size(); i++){
	        	String engineId = engineList.get(i);
	        	Engine c = manager.getById(engineId);
	            String xmlComment = c.getComment();
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
	            c.setComment(buf.toString());
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
        
        EngineManager manager = EngineManager.instance();
       	if (root.getChild("options") != null) {
    		Element e = root.getChild("options");
    		manager.options(e);
    	}
       	
        if (root.getChild("engineModels")!= null){
        	String names = root.getChildText("engineModels");
        	String[] models = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("engine models: "+names);
        	EngineModels.instance().setNames(models);
        }
        
        if (root.getChild("engineTypes")!= null){
        	String names = root.getChildText("engineTypes");
        	String[] types = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("engine types: "+names);
        	EngineTypes.instance().setNames(types);
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
        	@SuppressWarnings("unchecked")
            List<Element> l = root.getChild("engines").getChildren("engine");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" engines");
            for (int i=0; i<l.size(); i++) {
                manager.register(new Engine(l.get(i)));
            }

            List<String> engineList = manager.getByRoadNameList();
            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < engineList.size(); i++) {
                //Get a RosterEntry object for this index
            	String engineId = engineList.get(i);
	        	Engine c = manager.getById(engineId);

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
    public void setDirty(boolean b) {dirty = b;}
    boolean isDirty() {return dirty;}

    public void writeFileIfDirty(){
    	if(isDirty())
    		writeOperationsEngineFile();
    }
    
    // Operation files always use the same directory
    public static String defaultOperationsFilename() { 
    	return OperationsXml.getFileLocation()+OperationsXml.getOperationsDirectoryName()+File.separator+getOperationsFileName();
    }

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    public static String getOperationsFileName(){
    	return OperationsFileName;
    }
    private static String OperationsFileName = "OperationsEngineRoster.xml";

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineManagerXml.class.getName());

}
