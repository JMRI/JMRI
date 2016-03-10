// CarManagerXml.java
package jmri.jmrit.operations.rollingstock.engines;

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
 * Loads and stores engines using xml files. Also loads and stores engine
 * models, engine types, engine lengths, and engine consist names.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class EngineManagerXml extends OperationsXml {

    public EngineManagerXml() {
    }

    /**
     * record the single instance *
     */
    private static EngineManagerXml _instance = null;

    public static synchronized EngineManagerXml instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("EngineManagerXml creating instance");
            }
            // create and load
            _instance = new EngineManagerXml();
            _instance.load();
        }
        if (Control.showInstance) {
            log.debug("EngineManagerXml returns instance {}", _instance);
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
        Document doc = newDocument(root, dtdLocation + "operations-engines.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-engines.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        EngineModels.instance().store(root);
        EngineTypes.instance().store(root);
        EngineLengths.instance().store(root);
        EngineManager.instance().store(root);

        writeXML(file, doc);

        // done - engine file now stored, so can't be dirty
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

        EngineModels.instance().load(root);
        EngineTypes.instance().load(root);
        EngineLengths.instance().load(root);
        EngineManager.instance().load(root);

        log.debug("Engines have been loaded!");
        RollingStockLogger.instance().enableEngineLogging(Setup.isEngineLoggerEnabled());
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

    private String operationsFileName = "OperationsEngineRoster.xml"; // NOI18N

    public void dispose(){
        _instance = null;
    }


    private final static Logger log = LoggerFactory.getLogger(EngineManagerXml.class.getName());

}
