// RouteManagerXml.java

package jmri.jmrit.operations.routes;

import java.io.File;
import java.util.List;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Loads and stores routes using xml files. 
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 * @version $Revision: 1.19 $
 */
public class RouteManagerXml extends XmlFile {
	
	public RouteManagerXml(){
		
	}
	
	/** record the single instance **/
	private static RouteManagerXml _instance = null;

	public static synchronized RouteManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("RouteManagerXml creating instance");
			// create and load
			_instance = new RouteManagerXml();
	           try {
	                _instance.readFile(defaultOperationsFilename());
	            } catch (Exception e) {
	                log.error("Exception during operations route file reading: "+e);
	            }
				log.debug("Routes have been loaded!");
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("RouteManagerXml returns instance "+_instance);
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
	        Document doc = newDocument(root, dtdLocation+"operations-routes.dtd");

	        // add XSLT processing instruction
	        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
	        m.put("type", "text/xsl");
	        m.put("href", xsltLocation+"operations-routes.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);
	        
	        //Check the Comment and Decoder Comment fields for line breaks and
	        //convert them to a processor directive for storage in XML
	        RouteManager manager = RouteManager.instance();
	        List<String> routeList = manager.getRoutesByIdList();
	        
	        for (int i=0; i<routeList.size(); i++){

	            //Extract the RosterEntry at this index and inspect the Comment and
	            //Decoder Comment fields to change any \n characters to <?p?> processor
	            //directives so they can be stored in the xml file and converted
	            //back when the file is read.
	        	String routeId = routeList.get(i);
	        	Route route = manager.getRouteById(routeId);
	            String tempComment = route.getComment();
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
	            route.setComment(buf.toString());
	        }
	        //All Comments and Decoder Comment line feeds have been changed to processor directives


	        // add top-level elements
	        Element values;

	        root.addContent(values = new Element("routes"));
	        // add entries
	        for (int i=0; i<routeList.size(); i++) {
	        	String routeId = routeList.get(i);
	        	Route route = manager.getRouteById(routeId);
 	            values.addContent(route.store());
	        }
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state for the
	        //Comment and Decoder comment fields, otherwise it can cause problems in
	        //other parts of the program (e.g. in copying a roster)
	        for (int i=0; i<routeList.size(); i++){
	        	String routeId = routeList.get(i);
	        	Route route = manager.getRouteById(routeId);
	            String xmlComment = route.getComment();
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
	            route.setComment(buf.toString());
	        }

	        // done - route file now stored, so can't be dirty
	        setDirty(false);
	    }

	
	/**
     * Store the all of the operation route objects in the default place, including making a backup if needed
     */
    public void writeOperationsRouteFile() {
    	makeBackupFile(defaultOperationsFilename());
        try {
          	 if(!RouteManagerXml.instance().checkFile(defaultOperationsFilename())){
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
        	RouteManagerXml.instance().writeFile(defaultOperationsFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new operations file, may not be complete: "+e);
        }
    }
    
    public void writeFileIfDirty(){
    	if(isDirty())
    		writeOperationsRouteFile();
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
        
        RouteManager manager = RouteManager.instance();


        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("routes") != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = root.getChild("routes").getChildren("route");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" routes");
            for (int i=0; i<l.size(); i++) {
                manager.register(new Route(l.get(i)));
            }

            List<String> routeList = manager.getRoutesByIdList();
            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < routeList.size(); i++) {
                //Get a RosterEntry object for this index
	        	Route route = manager.getRouteById(routeList.get(i));

                //Extract the Comment field and create a new string for output
                String tempComment = route.getComment();
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
                route.setComment(buf.toString());
            }
        }
        else {
            log.error("Unrecognized operations route file contents in file: "+name);
        }
    }

    private boolean dirty = false;
    public void setDirty(boolean b) {dirty = b;}
    public boolean isDirty() {return dirty;}

    // Operation files always use the same directory
    public static String defaultOperationsFilename() { 
    	return OperationsXml.getFileLocation()+OperationsXml.getOperationsDirectoryName()+File.separator+getOperationsFileName();
    }

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    public static String getOperationsFileName(){
    	return OperationsFileName;
    }
    private static String OperationsFileName = "OperationsRouteRoster.xml";

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RouteManagerXml.class.getName());

}
