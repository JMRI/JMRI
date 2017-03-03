package jmri.jmrit.operations.routes;

import java.io.File;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and stores routes using xml files.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 */
public class RouteManagerXml extends OperationsXml {

    public RouteManagerXml() {
    }

    /**
     * record the single instance 
     * @return instance
     */

    public static synchronized RouteManagerXml instance() {
        RouteManagerXml instance = jmri.InstanceManager.getNullableDefault(RouteManagerXml.class);
        if (instance == null) {
            log.debug("RouteManagerXml creating instance");
            // create and load
            instance = new RouteManagerXml();
            jmri.InstanceManager.setDefault(RouteManagerXml.class,instance);
            instance.load();
            log.debug("Routes have been loaded!");
        }
        if (Control.SHOW_INSTANCE) {
            log.debug("RouteManagerXml returns instance {}", instance);
        }
        return instance;
    }

    @Override
    public void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
        log.debug("writeFile {}", name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }
        // create root element
        Element root = new Element("operations-config"); // NOI18N
        Document doc = newDocument(root, dtdLocation + "operations-routes.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-routes.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        RouteManager.instance().store(root);

        writeXML(file, doc);

        // done - route file now stored, so can't be dirty
        setDirty(false);
    }

    /**
     * Read the contents of a roster XML file into this object. Note that this
     * does not clear any existing entries.
     */
    @Override
    public void readFile(String name) throws org.jdom2.JDOMException, java.io.IOException {
        // suppress rootFromName(name) warning message by checking to see if file exists
        if (findFile(name) == null) {
            log.debug("{} file could not be found", name);
            return;
        }
        // find root
        Element root = rootFromName(name);
        if (root == null) {
            log.debug("{} file could not be read", name);
            return;
        }

        RouteManager.instance().load(root);

        // clear dirty bit
        setDirty(false);
    }

    @Override
    public void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    @Override
    public String getOperationsFileName() {
        return operationsFileName;
    }

    private String operationsFileName = "OperationsRouteRoster.xml"; // NOI18N

    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(RouteManagerXml.class.getName());

}
