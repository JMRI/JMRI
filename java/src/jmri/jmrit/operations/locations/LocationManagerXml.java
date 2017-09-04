package jmri.jmrit.operations.locations;

import java.io.File;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load and stores locations and schedules for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008 2009 2010
 */
public class LocationManagerXml extends OperationsXml implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    public LocationManagerXml() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized LocationManagerXml instance() {
        return InstanceManager.getDefault(LocationManagerXml.class);
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
        Document doc = newDocument(root, dtdLocation + "operations-locations.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-locations.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        InstanceManager.getDefault(LocationManager.class).store(root);
        InstanceManager.getDefault(ScheduleManager.class).store(root);

        writeXML(file, doc);

        // done - location file now stored, so can't be dirty
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

        InstanceManager.getDefault(LocationManager.class).load(root);
        InstanceManager.getDefault(ScheduleManager.class).load(root);

        setDirty(false);
        log.debug("Locations have been loaded!");
    }

    @Override
    public void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    @Override
    public String getOperationsFileName() {
        return operationsFileName;
    }

    private String operationsFileName = "OperationsLocationRoster.xml"; // NOI18N

    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(LocationManagerXml.class);

    @Override
    public void initialize() {
        this.load();
    }

}
