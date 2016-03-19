// TrainManagerXml.java
package jmri.jmrit.operations.trains;

import java.io.File;
import java.text.SimpleDateFormat;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and stores trains using xml files. Also stores various train parameters
 * managed by the TrainManager.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2010, 2015
 * @version $Revision$
 */
public class TrainManagerXml extends OperationsXml {

    private boolean fileLoaded = false;

    private String operationsFileName = "OperationsTrainRoster.xml";// NOI18N
    private String buildReportFileName = Bundle.getMessage("train") + " (";
    private String manifestFileName = Bundle.getMessage("train") + " (";
    private String switchListFileName = Bundle.getMessage("location") + " (";
    private String fileType = ").txt"; // NOI18N
    private String fileTypeCsv = ").csv"; // NOI18N

    // the directories under operations
    static final String BUILD_STATUS = "buildstatus"; // NOI18N
    static final String MANIFESTS = "manifests"; // NOI18N
    static final String SWITCH_LISTS = "switchLists"; // NOI18N
    static final String CSV_MANIFESTS = "csvManifests"; // NOI18N
    static final String CSV_SWITCH_LISTS = "csvSwitchLists"; // NOI18N
    static final String JSON_MANIFESTS = "jsonManifests"; // NOI18N
    static final String MANIFESTS_BACKUPS = "manifestsBackups"; // NOI18N

    public TrainManagerXml() {
    }

    /**
     * record the single instance *
     */
    private static TrainManagerXml _instance = null;

    public static synchronized TrainManagerXml instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("TrainManagerXml creating instance");
            }
            // create and load
            _instance = new TrainManagerXml();
            _instance.load();
        }
        if (Control.showInstance) {
            log.debug("TrainManagerXml returns instance " + _instance);
        }
        return _instance;
    }

    @Override
    public void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
        if (log.isDebugEnabled()) {
            log.debug("writeFile " + name);
        }
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }
        // create root element
        Element root = new Element("operations-config"); // NOI18N
        Document doc = newDocument(root, dtdLocation + "operations-trains.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-trains.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        TrainManager.instance().store(root);
        TrainScheduleManager.instance().store(root);
        AutomationManager.instance().store(root);

        writeXML(file, doc);

        // done - train file now stored, so can't be dirty
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
            log.debug(name + " file could not be found");
            fileLoaded = true; // set flag, could be the first time
            return;
        }
        // find root
        Element root = rootFromName(name);
        if (root == null) {
            log.debug(name + " file could not be read");
            return;
        }

        TrainManager.instance().load(root);
        TrainScheduleManager.instance().load(root);

        fileLoaded = true; // set flag trains are loaded
        AutomationManager.instance().load(root);

        // now load train icons on panels
        TrainManager.instance().loadTrainIcons();

        // loading complete run startup scripts
        TrainManager.instance().runStartUpScripts();

        log.debug("Trains have been loaded!");
        TrainLogger.instance().enableTrainLogging(Setup.isTrainLoggerEnabled());
        setDirty(false); // clear dirty flag
    }

    public boolean isTrainFileLoaded() {
        return fileLoaded;
    }

    /**
     * Store the train's build report
     */
    public File createTrainBuildReportFile(String name) {
        return createFile(defaultBuildReportFilename(name), false); // don't backup
    }

    public File getTrainBuildReportFile(String name) {
        File file = new File(defaultBuildReportFilename(name));
        return file;
    }

    public String defaultBuildReportFilename(String name) {
        return OperationsXml.getFileLocation() + OperationsXml.getOperationsDirectoryName() + File.separator
                + BUILD_STATUS + File.separator + buildReportFileName + name + fileType; // NOI18N
    }

    public void setBuildReportName(String name) {
        buildReportFileName = name;
    }

    /**
     * Store the train's manifest
     */
    public File createTrainManifestFile(String name) {
        savePreviousManifestFile(name);
        return createFile(getDefaultManifestFilename(name), false); // don't backup
    }

    public File getTrainManifestFile(String name) {
        File file = new File(getDefaultManifestFilename(name));
        return file;
    }

    public String getDefaultManifestFilename(String name) {
        return OperationsXml.getFileLocation() + OperationsXml.getOperationsDirectoryName() + File.separator + MANIFESTS
                + File.separator + manifestFileName + name + fileType;// NOI18N
    }
    
    public String getBackupManifestFilename(String name, String lastModified) {
        return getBackupManifestDirectory() + manifestFileName + name + ") " + lastModified + ".txt";// NOI18N
    }
    
    public String getBackupManifestDirectory() {
        return OperationsXml.getFileLocation() + OperationsXml.getOperationsDirectoryName() + File.separator 
                + MANIFESTS_BACKUPS + File.separator;
    }
    
    /**
     * Store the CSV train manifest
     */
    public File createTrainCsvManifestFile(String name) {
        return createFile(getDefaultCsvManifestFilename(name), false); // don't backup
    }
    
    public File getTrainCsvManifestFile(String name) {
        File file = new File(getDefaultCsvManifestFilename(name));
        return file;
    }

    public String getDefaultCsvManifestFilename(String name) {
        return defaultCsvManifestDirectory + manifestFileName + name + fileTypeCsv; // NOI18N
    }

    private String defaultCsvManifestDirectory = OperationsXml.getFileLocation()
            + OperationsXml.getOperationsDirectoryName() + File.separator + CSV_MANIFESTS + File.separator;

    public void createDefaultCsvManifestDirectory() {
        createFile(defaultCsvManifestDirectory + " ", false); // don't backup
    }

    /**
     * Store the Json manifest for a train
     */
    public File createManifestFile(String name, String ext) {
        return createFile(getDefaultManifestFilename(name, ext), false); // don't backup
    }
    
    public File getManifestFile(String name, String ext) {
        return new File(getDefaultManifestFilename(name, ext));
    }

    private String getDefaultManifestFilename(String name, String ext) {
        return OperationsManager.getInstance().getPath(JSON_MANIFESTS) + File.separator + "train-" + name + "." + ext; // NOI18N
    }

    /**
     * Store the switch list for a location
     */
    public File createSwitchListFile(String name) {
        return createFile(getDefaultSwitchListName(name), false); // don't backup
    }

    public File getSwitchListFile(String name) {
        File file = new File(getDefaultSwitchListName(name));
        return file;
    }

    public String getDefaultSwitchListName(String name) {
        return OperationsXml.getFileLocation() + OperationsXml.getOperationsDirectoryName() + File.separator
                + SWITCH_LISTS + File.separator + switchListFileName + name + fileType; // NOI18N
    }

    /**
     * Store the CSV switch list for a location
     */
    public File createCsvSwitchListFile(String name) {
        return createFile(getDefaultCsvSwitchListName(name), false); // don't backup
    }

    public File getCsvSwitchListFile(String name) {
        File file = new File(getDefaultCsvSwitchListName(name));
        return file;
    }

    public String getDefaultCsvSwitchListName(String name) {
        return defaultCsvSwitchListDirectory + switchListFileName + name + fileTypeCsv;// NOI18N
    }

    private String defaultCsvSwitchListDirectory = OperationsXml.getFileLocation()
            + OperationsXml.getOperationsDirectoryName() + File.separator + CSV_SWITCH_LISTS + File.separator;

    public void createDefaultCsvSwitchListDirectory() {
        createFile(defaultCsvSwitchListDirectory + " ", false); // don't backup
    }

    public void setTrainSwitchListName(String name) {
        switchListFileName = name;
    }

    @Override
    public void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    @Override
    public String getOperationsFileName() {
        return operationsFileName;
    }
    
    /**
     * Save previous manifest file in a separate directory called manifestBackups
     * @param name
     */
    private void savePreviousManifestFile(String name) {
        if (Setup.isSaveTrainManifestsEnabled()) {
            // create the manifest backup directory
            createFile (getBackupManifestDirectory() + " ", false); // no backup
            // get old manifest file
            File file = findFile(getDefaultManifestFilename(name));
            if (file == null) {
                log.debug("No {} manifest file to backup", name);
            } else if (file.canWrite()) {
                String lastModified = new SimpleDateFormat("yyyyMMdd-HHmmss").format(file.lastModified());
                String backupName = getBackupManifestFilename(name, lastModified); // NOI18N
                if (file.renameTo(new File(backupName))) {
                    log.debug("created new manifest backup file {}", backupName);
                } else {
                    log.error("could not create manifest backup file {}", backupName);
                }
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void dispose(){
        _instance = null;
    }

    private final static Logger log = LoggerFactory.getLogger(TrainManagerXml.class.getName());

}
