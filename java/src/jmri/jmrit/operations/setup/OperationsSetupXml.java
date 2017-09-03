package jmri.jmrit.operations.setup;

import java.io.File;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.trains.TrainManifestHeaderText;
import jmri.jmrit.operations.trains.TrainManifestText;
import jmri.jmrit.operations.trains.TrainSwitchListText;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and stores the operation setup using xml files.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 */
public class OperationsSetupXml extends OperationsXml implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    public OperationsSetupXml() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized OperationsSetupXml instance() {
        return InstanceManager.getDefault(OperationsSetupXml.class);
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
        Document doc = newDocument(root, dtdLocation + "operations-config.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-config.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        // add top-level elements
        root.addContent(Setup.store());
        // add manifest header text strings
        root.addContent(TrainManifestHeaderText.store());
        // add manifest text strings
        root.addContent(TrainManifestText.store());
        // add switch list text strings
        root.addContent(TrainSwitchListText.store());
        // add control elements
        root.addContent(Control.store());

        writeXML(file, doc);

        // done, so can't be dirty
        setDirty(false);
    }

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
        Setup.load(root);
        // load manifest header text strings
        TrainManifestHeaderText.load(root);
        // load manifest text strings
        TrainManifestText.load(root);
        // load switch list text strings
        TrainSwitchListText.load(root);
        // load control settings
        Control.load(root);
    }

    @Override
    public void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    @Override
    public String getOperationsFileName() {
        return operationsFileName;
    }

    private String operationsFileName = "Operations.xml"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(OperationsSetupXml.class);

    @Override
    public void initialize() {
        load();
    }
}
