package jmri.jmrit.operations.rollingstock.engines;

import java.io.File;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
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
 */
public class EngineManagerXml extends OperationsXml implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    public EngineManagerXml() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized EngineManagerXml instance() {
        return InstanceManager.getDefault(EngineManagerXml.class);
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
        Document doc = newDocument(root, dtdLocation + "operations-engines.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-engines.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        InstanceManager.getDefault(EngineModels.class).store(root);
        InstanceManager.getDefault(EngineTypes.class).store(root);
        InstanceManager.getDefault(EngineLengths.class).store(root);
        InstanceManager.getDefault(EngineManager.class).store(root);

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

        InstanceManager.getDefault(EngineModels.class).load(root);
        InstanceManager.getDefault(EngineTypes.class).load(root);
        InstanceManager.getDefault(EngineLengths.class).load(root);
        InstanceManager.getDefault(EngineManager.class).load(root);

        log.debug("Engines have been loaded!");
        InstanceManager.getDefault(RollingStockLogger.class).enableEngineLogging(Setup.isEngineLoggerEnabled());
        // clear dirty bit
        setDirty(false);
        // clear location dirty flag, locations get modified during the loading of cars and locos
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(false);
    }

    @Override
    public void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    @Override
    public String getOperationsFileName() {
        return operationsFileName;
    }

    private String operationsFileName = "OperationsEngineRoster.xml"; // NOI18N

    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(EngineManagerXml.class);

    @Override
    public void initialize() {
        load();
    }

}
