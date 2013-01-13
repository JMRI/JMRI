// RouteManagerXml.java

package jmri.jmrit.operations.routes;

import java.io.File;
import java.util.List;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.OperationsXml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Loads and stores routes using xml files. 
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class RouteManagerXml extends OperationsXml {
	
	public RouteManagerXml(){
	}
	
	/** record the single instance **/
	private static RouteManagerXml _instance = null;

	public static synchronized RouteManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("RouteManagerXml creating instance");
			// create and load
			_instance = new RouteManagerXml();
			_instance.load();
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
		Element root = new Element("operations-config"); // NOI18N
		Document doc = newDocument(root, dtdLocation+"operations-routes.dtd"); // NOI18N

		// add XSLT processing instruction
		java.util.Map<String, String> m = new java.util.HashMap<String, String>();
		m.put("type", "text/xsl"); // NOI18N
		m.put("href", xsltLocation+"operations-routes.xsl"); // NOI18N
		ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
		doc.addContent(0,p);

		// add top-level elements
		Element values = new Element(Xml.ROUTES);
		root.addContent(values);
		// add entries
		RouteManager manager = RouteManager.instance();
		List<String> routeList = manager.getRoutesByIdList();
		for (int i=0; i<routeList.size(); i++) {
			Route route = manager.getRouteById(routeList.get(i));
			values.addContent(route.store());
		}
		writeXML(file, doc);

		// done - route file now stored, so can't be dirty
		setDirty(false);
	}

    
    /**
     * Read the contents of a roster XML file into this object. Note that this does not
     * clear any existing entries.
     */
    public void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
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
        
        RouteManager manager = RouteManager.instance();

        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild(Xml.ROUTES) != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = root.getChild(Xml.ROUTES).getChildren(Xml.ROUTE);
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" routes");
            for (int i=0; i<l.size(); i++) {
                manager.register(new Route(l.get(i)));
            }
        }
        else {
            log.error("Unrecognized operations route file contents in file: "+name);
        }
        // clear dirty bit
        setDirty(false);
    }

    public void setOperationsFileName(String name) { operationsFileName = name; }
	public String getOperationsFileName(){
		return operationsFileName;
	}
	
    private String operationsFileName = "OperationsRouteRoster.xml"; // NOI18N

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RouteManagerXml.class.getName());

}
