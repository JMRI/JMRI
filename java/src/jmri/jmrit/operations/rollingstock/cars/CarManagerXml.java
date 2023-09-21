package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

/**
 * Loads and stores cars using xml files. Also loads and stores car road names,
 * car types, car colors, car lengths, car owners, and car kernels.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class CarManagerXml extends OperationsXml implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {
    
    // the directories under operations
    static final String CAR_ROUTER_REPORTS = "carRouterReports"; // NOI18N
    // sub directory under CAR_ROUTER_REPORTS
    static final String CAR_ROUTER_REPORTS_RAW = "carRouterReports";
    
    public CarManagerXml() {
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
        Document doc = newDocument(root, dtdLocation + "operations-cars.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-cars.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        // note all comments line feeds have been changed to processor directives
        InstanceManager.getDefault(CarRoads.class).store(root);
        InstanceManager.getDefault(CarTypes.class).store(root);
        InstanceManager.getDefault(CarColors.class).store(root);
        InstanceManager.getDefault(CarLengths.class).store(root);
        InstanceManager.getDefault(CarOwners.class).store(root);
        InstanceManager.getDefault(CarLoads.class).store(root);
        InstanceManager.getDefault(KernelManager.class).store(root);
        InstanceManager.getDefault(CarManager.class).store(root);

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

        InstanceManager.getDefault(CarRoads.class).load(root);
        InstanceManager.getDefault(CarTypes.class).load(root);
        InstanceManager.getDefault(CarColors.class).load(root);
        InstanceManager.getDefault(CarLengths.class).load(root);
        InstanceManager.getDefault(CarOwners.class).load(root);
        InstanceManager.getDefault(CarLoads.class).load(root);
        InstanceManager.getDefault(KernelManager.class).load(root);
        InstanceManager.getDefault(CarManager.class).load(root);

        log.debug("Cars have been loaded!");
        InstanceManager.getDefault(RollingStockLogger.class).enableCarLogging(Setup.isCarLoggerEnabled());
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

    private String operationsFileName = "OperationsCarRoster.xml"; // NOI18N
    
    public File createCarRouterReportFile(String name) {
        return createFile(defaultCarRouterReportFileName(name), false); // don't backup
    }

    public File getCarRouterReportFile(String name) {
        File file = new File(defaultCarRouterReportFileName(name));
        return file;
    }

    public String defaultCarRouterReportFileName(String name) {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + CAR_ROUTER_REPORTS
                + File.separator
                + Bundle.getMessage("RoutingReportCar", name);
    }
    
    public File createRawCarRouterReportFile(String name) {
        // must create 1st level directory first
        createFile(defaultCarRouterReportFileName(name), false);
        return createFile(defaultRawCarRouterReportFileName(name), false); // don't backup
    }

    public File getRawCarRouterReportFile(String name) {
        File file = new File(defaultRawCarRouterReportFileName(name));
        return file;
    }

    public String defaultRawCarRouterReportFileName(String name) {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + CAR_ROUTER_REPORTS
                + File.separator
                + CAR_ROUTER_REPORTS_RAW
                + File.separator
                + Bundle.getMessage("RoutingReportCar", name);
    }


    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(CarManagerXml.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load setup
        InstanceManager.getDefault(LocationManagerXml.class); // load locations
        CarManagerXml.this.load();
    }
}
