// RouteManagerXml.java

package jmri.jmrit.operations.routes;

import java.io.File;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import jmri.jmrit.XmlFile;

import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.roster.Roster;

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
	                log.error("Exception during operations location file reading: "+e);
	            }
				log.debug("Routes have been loaded!");
		}
		if (log.isDebugEnabled()) log.debug("RouteManagerXml returns instance "+_instance);
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
	        Document doc = newDocument(root, dtdLocation+"operations-routes.dtd");

	        // add XSLT processing instruction
	        java.util.Map m = new java.util.HashMap();
	        m.put("type", "text/xsl");
	        m.put("href", "http://jmri.sourceforge.net/xml/XSLT/operations-routes.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);
	        
	        //Check the Comment and Decoder Comment fields for line breaks and
	        //convert them to a processor directive for storage in XML
	        //Note: this is also done in the LocoFile.java class to do
	        //the same thing in the indidvidual locomotive roster files
	        //Note: these changes have to be undone after writing the file
	        //since the memory version of the roster is being changed to the
	        //file version for writing
	        RouteManager manager = RouteManager.instance();
	        List routeList = manager.getRoutesByIdList();
	        
	        for (int i=0; i<routeList.size(); i++){

	            //Extract the RosterEntry at this index and inspect the Comment and
	            //Decoder Comment fields to change any \n characters to <?p?> processor
	            //directives so they can be stored in the xml file and converted
	            //back when the file is read.
	        	String routeId = (String)routeList.get(i);
	        	Route route = manager.getRouteById(routeId);
	            String tempComment = route.getComment();
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
	            route.setComment(xmlComment);
	        }
	        //All Comments and Decoder Comment line feeds have been changed to processor directives


	        // add top-level elements
	        Element values;

	        root.addContent(values = new Element("routes"));
	        // add entries
	        for (int i=0; i<routeList.size(); i++) {
	        	String routeId = (String)routeList.get(i);
	        	Route route = manager.getRouteById(routeId);
 	            values.addContent(route.store());
	        }
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state for the
	        //Comment and Decoder comment fields, otherwise it can cause problems in
	        //other parts of the program (e.g. in copying a roster)
	        for (int i=0; i<routeList.size(); i++){
	        	String routeId = (String)routeList.get(i);
	        	Route route = manager.getRouteById(routeId);
	            String xmlComment = route.getComment();
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
	            route.setComment(tempComment);
	        }

	        // done - route file now stored, so can't be dirty
	        setDirty(false);
	    }

	
	/**
     * Store the all of the operation route objects in the default place, including making a backup if needed
     */
    public static void writeOperationsRouteFile() {
    	RouteManagerXml.instance().makeBackupFile(defaultOperationsFilename());
        try {
          	 if(!RouteManagerXml.instance().checkFile(defaultOperationsFilename()))
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
        	RouteManagerXml.instance().writeFile(defaultOperationsFilename());
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
        
        RouteManager manager = RouteManager.instance();


        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("routes") != null) {
        	
            List l = root.getChild("routes").getChildren("route");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" routes");
            for (int i=0; i<l.size(); i++) {
                manager.register(new Route((Element)l.get(i)));
            }

            List routeList = manager.getRoutesByIdList();
            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < routeList.size(); i++) {
                //Get a RosterEntry object for this index
            	String routeId = (String)routeList.get(i);
	        	Route route = manager.getRouteById(routeId);

                //Extract the Comment field and create a new string for output
                String tempComment = route.getComment();
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
                route.setComment(xmlComment);
            }
        }
        else {
            log.error("Unrecognized operations route file contents in file: "+name);
        }
    }

    private boolean dirty = false;
    void setDirty(boolean b) {dirty = b;}
    boolean isDirty() {return dirty;}

    
    public static String defaultOperationsFilename() { return XmlFile.prefsDir()+"operations"+File.separator+OperationsFileName;}

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    private static String OperationsFileName = "OperationsRouteRoster.xml";

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RouteManagerXml.class.getName());

}
