// CarManagerXml.java

package jmri.jmrit.operations.rollingstock.engines;

import java.io.File;
import java.util.List;

import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.OperationsXml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Loads and stores engines using xml files. Also loads and stores engine
 * models, engine types, engine lengths, and engine consist names.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class EngineManagerXml extends OperationsXml {
	
	public EngineManagerXml(){
	}
	
	/** record the single instance **/
	private static EngineManagerXml _instance = null;

	public static synchronized EngineManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineManagerXml creating instance");
			// create and load
			_instance = new EngineManagerXml();
			_instance.load();
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
		Element root = new Element("operations-config"); // NOI18N
		Document doc = newDocument(root, dtdLocation+"operations-engines.dtd"); // NOI18N

		// add XSLT processing instruction
		java.util.Map<String, String> m = new java.util.HashMap<String, String>();
		m.put("type", "text/xsl"); // NOI18N
		m.put("href", xsltLocation+"operations-engines.xsl"); // NOI18N
		ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
		doc.addContent(0,p);

		// add top-level elements
		EngineManager manager = EngineManager.instance();
		root.addContent(manager.store());
		Element values;
		root.addContent(values = new Element(Xml.ENGINE_MODELS));
		String[]models = EngineModels.instance().getNames();
		for (int i=0; i<models.length; i++){
			String typeNames = models[i]+"%%"; // NOI18N
			values.addContent(typeNames);
		}
		root.addContent(values = new Element(Xml.ENGINE_TYPES));
		String[]types = EngineTypes.instance().getNames();
		for (int i=0; i<types.length; i++){
			String typeNames = types[i]+"%%"; // NOI18N
			values.addContent(typeNames);
		}
		root.addContent(values = new Element(Xml.ENGINE_LENGTHS));
		String[]lengths = EngineLengths.instance().getNames();
		for (int i=0; i<lengths.length; i++){
			String lengthNames = lengths[i]+"%%"; // NOI18N
			values.addContent(lengthNames);
		}
		root.addContent(values = new Element(Xml.CONSISTS));
		List<String> consists = manager.getConsistNameList();
		for (int i=0; i<consists.size(); i++){
			String consistNames = consists.get(i)+"%%"; // NOI18N
			values.addContent(consistNames);
		}
		root.addContent(values = new Element(Xml.ENGINES));
		// add entries
		List<String> engineList = manager.getByRoadNameList();
		for (int i=0; i<engineList.size(); i++) {
			Engine e = manager.getById(engineList.get(i));
			e.setComment(convertToXmlComment(e.getComment()));
			values.addContent(e.store());
		}
		writeXML(file, doc);

		//Now that the roster has been rewritten in file form we need to
		//restore the normal \n state for the comment fields
		for (int i=0; i<engineList.size(); i++){
			Engine e = manager.getById(engineList.get(i));
			e.setComment(convertToXmlComment(e.getComment()));
		}
		// done - engine file now stored, so can't be dirty
		setDirty(false);
	}
    
    /**
     * Read the contents of a roster XML file into this object. Note that this does not
     * clear any existing entries.
     */
    protected void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
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
        
        EngineManager manager = EngineManager.instance();
       	if (root.getChild(Xml.OPTIONS) != null) {
    		Element e = root.getChild(Xml.OPTIONS);
    		manager.options(e);
    	}
       	
        if (root.getChild(Xml.ENGINE_MODELS)!= null){
        	String names = root.getChildText(Xml.ENGINE_MODELS);
        	String[] models = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("engine models: "+names);
        	EngineModels.instance().setNames(models);
        }
        
        if (root.getChild(Xml.ENGINE_TYPES)!= null){
        	String names = root.getChildText(Xml.ENGINE_TYPES);
        	String[] types = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("engine types: "+names);
        	EngineTypes.instance().setNames(types);
        }
          
        if (root.getChild(Xml.ENGINE_LENGTHS)!= null){
        	String names = root.getChildText(Xml.ENGINE_LENGTHS);
        	String[] lengths = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("engine lengths: "+names);
        	EngineLengths.instance().setNames(lengths);
        }
        
        if (root.getChild(Xml.CONSISTS)!= null){
        	String names = root.getChildText(Xml.CONSISTS);
        	if(!names.equals("")){
        		String[] consistNames = names.split("%%"); // NOI18N
        		if (log.isDebugEnabled()) log.debug("consists: "+names);
        		for (int i=0; i<consistNames.length; i++){
        			manager.newConsist(consistNames[i]);
        		}
        	}
        }
         
        if (root.getChild(Xml.ENGINES) != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = root.getChild(Xml.ENGINES).getChildren(Xml.ENGINE);
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
                StringBuffer buf = new StringBuffer();

                //transfer tempComment to xmlComment one character at a time, except
                //when <?p?> is found.  In that case, insert a \n and skip over those
                //characters in tempComment.
                for (int k = 0; k < tempComment.length(); k++) {
                    if (tempComment.startsWith("<?p?>", k)) { // NOI18N
                        buf.append("\n"); // NOI18N
                        k = k + 4;
                    }
                    else {
                    	buf.append(tempComment.substring(k, k + 1));
                    }
                }
                c.setComment(buf.toString());
            }
        }
        else {
            log.error("Unrecognized operations engine file contents in file: "+name);
        }
		log.debug("Engines have been loaded!");
		RollingStockLogger.instance().enableEngineLogging(Setup.isEngineLoggerEnabled());
		// clear dirty bit
		setDirty(false);
		// clear location dirty flag, locations get modified during the loading of cars and locos
		LocationManagerXml.instance().setDirty(false);
    }
    
    public void setOperationsFileName(String name) { operationsFileName = name; }
	public String getOperationsFileName(){
		return operationsFileName;
	}
 
    private String operationsFileName = "OperationsEngineRoster.xml"; // NOI18N

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineManagerXml.class.getName());

}
