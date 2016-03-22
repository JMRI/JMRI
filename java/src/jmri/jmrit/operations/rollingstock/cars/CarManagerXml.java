// CarManagerXml.java
package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and stores cars using xml files. Also loads and stores car road names,
 * car types, car colors, car lengths, car owners, and car kernels.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision$
 */
public class CarManagerXml extends OperationsXml {

    public CarManagerXml() {
    }

    /**
     * record the single instance *
     */
    private static CarManagerXml _instance = null;

    public static synchronized CarManagerXml instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("CarManagerXml creating instance");
            }
            // create and load
            _instance = new CarManagerXml();
            _instance.load();
        }
        if (Control.SHOW_INSTANCE) {
            log.debug("CarManagerXml returns instance {}", _instance);
        }
        return _instance;
    }

    public void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
        if (log.isDebugEnabled()) {
            log.debug("writeFile {}", name);
        }
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }
        // create root element
        Element root = new Element("operations-config"); // NOI18N
        Document doc = newDocument(root, dtdLocation + "operations-cars.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-cars.xsl");	// NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        // note all comments line feeds have been changed to processor directives
        CarRoads.instance().store(root);
        CarTypes.instance().store(root);
        CarColors.instance().store(root);
        CarLengths.instance().store(root);
        CarOwners.instance().store(root);
        CarLoads.instance().store(root);
        CarManager.instance().store(root);

        writeXML(file, doc);

        // done - car file now stored, so can't be dirty
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

        CarRoads.instance().load(root);
        CarTypes.instance().load(root);
        CarColors.instance().load(root);
        CarLengths.instance().load(root);
        CarOwners.instance().load(root);
        CarLoads.instance().load(root);
        CarManager.instance().load(root);

        log.debug("Cars have been loaded!");
        RollingStockLogger.instance().enableCarLogging(Setup.isCarLoggerEnabled());
        // clear dirty bit
        setDirty(false);
        // clear location dirty flag, locations get modified during the loading of cars and locos
        LocationManagerXml.instance().setDirty(false);
    }

    public void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    public String getOperationsFileName() {
        return operationsFileName;
    }
    private String operationsFileName = "OperationsCarRoster.xml"; // NOI18N

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "for testing")
    public void dispose(){
        _instance = null;
    }


    private final static Logger log = LoggerFactory.getLogger(CarManagerXml.class.getName());

}
