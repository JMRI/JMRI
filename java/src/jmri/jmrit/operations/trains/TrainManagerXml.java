package jmri.jmrit.operations.trains;

import java.io.File;
import java.text.SimpleDateFormat;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.util.FileUtil;
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
 */
public class TrainManagerXml extends OperationsXml implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {

    private boolean fileLoaded = false;
    private String operationsFileName = "OperationsTrainRoster.xml";// NOI18N

    private static final String BUILD_REPORT_FILE_NAME = Bundle.getMessage("train") + " (";
    private static final String MANIFEST_FILE_NAME = Bundle.getMessage("train") + " (";
    private static final String SWITCH_LIST_FILE_NAME = Bundle.getMessage("location") + " (";
    private static final String BACKUP_BUILD_REPORT_FILE_NAME = Bundle.getMessage("Report") + " " + Bundle.getMessage("train") + " (";
    private static final String FILE_TYPE_TXT = ").txt"; // NOI18N
    private static final String FILE_TYPE_CSV = ").csv"; // NOI18N

    // the directories under operations
    static final String BUILD_STATUS = "buildstatus"; // NOI18N
    static final String MANIFESTS = "manifests"; // NOI18N
    static final String SWITCH_LISTS = "switchLists"; // NOI18N
    static final String CSV_MANIFESTS = "csvManifests"; // NOI18N
    static final String CSV_SWITCH_LISTS = "csvSwitchLists"; // NOI18N
    static final String JSON_MANIFESTS = "jsonManifests"; // NOI18N
    static final String MANIFESTS_BACKUPS = "manifestsBackups"; // NOI18N
    static final String SWITCH_LISTS_BACKUPS = "switchListsBackups"; // NOI18N
    static final String BUILD_STATUS_BACKUPS = "buildStatusBackups"; // NOI18N

    public TrainManagerXml() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized TrainManagerXml instance() {
        return InstanceManager.getDefault(TrainManagerXml.class);
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
        Document doc = newDocument(root, dtdLocation + "operations-trains.dtd"); // NOI18N

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "operations-trains.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        InstanceManager.getDefault(TrainManager.class).store(root);
        InstanceManager.getDefault(TrainScheduleManager.class).store(root);
        InstanceManager.getDefault(AutomationManager.class).store(root);

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

        InstanceManager.getDefault(TrainManager.class).load(root);
        InstanceManager.getDefault(TrainScheduleManager.class).load(root);

        fileLoaded = true; // set flag trains are loaded
        InstanceManager.getDefault(AutomationManager.class).load(root);

        // now load train icons on panels
        InstanceManager.getDefault(TrainManager.class).loadTrainIcons();

        // loading complete run startup scripts
        InstanceManager.getDefault(TrainManager.class).runStartUpScripts();

        log.debug("Trains have been loaded!");
        InstanceManager.getDefault(TrainLogger.class).enableTrainLogging(Setup.isTrainLoggerEnabled());
        setDirty(false); // clear dirty flag
    }

    public boolean isTrainFileLoaded() {
        return fileLoaded;
    }

    /**
     * Store the train's build report
     *
     * @param name Full path name for train build report
     * @return Build report File.
     */
    public File createTrainBuildReportFile(String name) {
        return createFile(defaultBuildReportFileName(name), false); // don't backup
    }

    public File getTrainBuildReportFile(String name) {
        File file = new File(defaultBuildReportFileName(name));
        return file;
    }

    public String defaultBuildReportFileName(String name) {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + BUILD_STATUS
                + File.separator
                + BUILD_REPORT_FILE_NAME
                + name
                + FILE_TYPE_TXT; // NOI18N
    }

    /**
     * Creates the train's manifest file.
     *
     * @param name Full path name for manifest file.
     * @return Manifest File.
     */
    public File createTrainManifestFile(String name) {
        savePreviousManifestFile(name);
        return createFile(getDefaultManifestFileName(name), false); // don't backup
    }

    public File getTrainManifestFile(String name) {
        File file = new File(getDefaultManifestFileName(name));
        return file;
    }

    public String getDefaultManifestFileName(String name) {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + MANIFESTS
                + File.separator
                + MANIFEST_FILE_NAME
                + name
                + FILE_TYPE_TXT;// NOI18N
    }

    public String getBackupManifestFileName(String name, String lastModified) {
        return getBackupManifestDirectoryName()
                + name
                + File.separator
                + MANIFEST_FILE_NAME
                + name
                + ") "
                + lastModified
                + ".txt";// NOI18N
    }

    public String getBackupManifestDirectoryName() {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + MANIFESTS_BACKUPS
                + File.separator;
    }

    public String getBackupManifestDirectoryName(String name) {
        return getBackupManifestDirectoryName() + File.separator + name + File.separator;
    }

    public String getBackupSwitchListFileName(String name, String lastModified) {
        return getBackupSwitchListDirectoryName()
                + name
                + File.separator
                + SWITCH_LIST_FILE_NAME
                + name
                + ") "
                + lastModified
                + ".txt";// NOI18N
    }

    public String getBackupSwitchListDirectoryName() {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + SWITCH_LISTS_BACKUPS
                + File.separator;
    }

    public String getBackupSwitchListDirectoryName(String name) {
        return getBackupSwitchListDirectoryName() + File.separator + name + File.separator;
    }
    
    public String getBackupBuildStatusFileName(String name, String lastModified) {
        return getBackupBuildStatusDirectoryName()
                + name
                + File.separator
                + BACKUP_BUILD_REPORT_FILE_NAME
                + name
                + ") "
                + lastModified
                + ".txt";// NOI18N
    }
    
    public String getBackupBuildStatusDirectoryName() {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + BUILD_STATUS_BACKUPS
                + File.separator;
    }
    
    public String getBackupBuildStatusDirectoryName(String name) {
        return getBackupBuildStatusDirectoryName() + File.separator + name + File.separator;
    }

    /**
     * Store the CSV train manifest
     *
     * @param name Full path name to CSV train manifest file.
     * @return Train CSV manifest File.
     */
    public File createTrainCsvManifestFile(String name) {
        return createFile(getDefaultCsvManifestFileName(name), false); // don't backup
    }

    public File getTrainCsvManifestFile(String name) {
        File file = new File(getDefaultCsvManifestFileName(name));
        return file;
    }

    public String getDefaultCsvManifestFileName(String name) {
        return getDefaultCsvManifestDirectory() + MANIFEST_FILE_NAME + name + FILE_TYPE_CSV;
    }

    private String getDefaultCsvManifestDirectory() {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + CSV_MANIFESTS
                + File.separator;
    }

    public void createDefaultCsvManifestDirectory() {
        FileUtil.createDirectory(getDefaultCsvManifestDirectory());
    }

    /**
     * Store the Json manifest for a train
     *
     * @param name file name
     * @param ext  file extension to use
     * @return Json manifest File
     */
    public File createManifestFile(String name, String ext) {
        return createFile(getDefaultManifestFileName(name, ext), false); // don't backup
    }

    public File getManifestFile(String name, String ext) {
        return new File(getDefaultManifestFileName(name, ext));
    }

    private String getDefaultManifestFileName(String name, String ext) {
        return InstanceManager.getDefault(OperationsManager.class).getPath(JSON_MANIFESTS) + File.separator + "train-" + name + "." + ext; // NOI18N
    }

    /**
     * Store the switch list for a location
     *
     * @param name The location's name, to become file name.
     * @return Switch list File.
     */
    public File createSwitchListFile(String name) {
        savePreviousSwitchListFile(name);
        return createFile(getDefaultSwitchListName(name), false); // don't backup
    }

    public File getSwitchListFile(String name) {
        File file = new File(getDefaultSwitchListName(name));
        return file;
    }

    public String getDefaultSwitchListName(String name) {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + SWITCH_LISTS
                + File.separator
                + SWITCH_LIST_FILE_NAME
                + name
                + FILE_TYPE_TXT; // NOI18N
    }

    /**
     * Store the CSV switch list for a location
     *
     * @param name Location's name, to become file name.
     * @return CSV switch list File.
     */
    public File createCsvSwitchListFile(String name) {
        return createFile(getDefaultCsvSwitchListFileName(name), false); // don't backup
    }

    public File getCsvSwitchListFile(String name) {
        File file = new File(getDefaultCsvSwitchListFileName(name));
        return file;
    }

    public String getDefaultCsvSwitchListFileName(String name) {
        return getDefaultCsvSwitchListDirectoryName() + SWITCH_LIST_FILE_NAME + name + FILE_TYPE_CSV;
    }

    private String getDefaultCsvSwitchListDirectoryName() {
        return OperationsXml.getFileLocation()
                + OperationsXml.getOperationsDirectoryName()
                + File.separator
                + CSV_SWITCH_LISTS
                + File.separator;
    }

    public void createDefaultCsvSwitchListDirectory() {
        FileUtil.createDirectory(getDefaultCsvSwitchListDirectoryName());
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
     * Save previous manifest file in a separate directory called
     * manifestBackups. Each train manifest is saved in a unique directory using
     * the train's name.
     */
    private void savePreviousManifestFile(String name) {
        if (Setup.isSaveTrainManifestsEnabled()) {
            // create the manifest backup directory
            createFile(getBackupManifestDirectoryName() + " ", false); // no backup
            // now create unique backup directory for each train manifest
            createFile(getBackupManifestDirectoryName(name) + " ", false); // no backup
            // get old manifest file
            File file = findFile(getDefaultManifestFileName(name));
            if (file == null) {
                log.debug("No ({}) manifest file to backup", name);
            } else if (file.canWrite()) {
                String lastModified = new SimpleDateFormat("yyyyMMdd-HHmmss").format(file.lastModified()); // NOI18N
                String backupName = getBackupManifestFileName(name, lastModified); // NOI18N
                if (file.renameTo(new File(backupName))) {
                    log.debug("created new manifest backup file {}", backupName);
                } else {
                    log.error("could not create manifest backup file {}", backupName);
                }
            }
        }
    }

    /**
     * Save previous switch list file in a separate directory called
     * switchListBackups. Each switch list is saved in a unique directory using
     * the location's name.
     */
    private void savePreviousSwitchListFile(String name) {
        if (Setup.isSaveTrainManifestsEnabled()) {
            // create the switch list backup directory
            createFile(getBackupSwitchListDirectoryName() + " ", false); // no backup
            // now create unique backup directory for location
            createFile(getBackupSwitchListDirectoryName(name) + " ", false); // no backup
            // get old switch list file
            File file = findFile(getDefaultSwitchListName(name));
            if (file == null) {
                log.debug("No ({}) switch list file to backup", name);
            } else if (file.canRead()) {
                String lastModified = new SimpleDateFormat("yyyyMMdd-HHmmss").format(file.lastModified()); // NOI18N
                String backupName = getBackupSwitchListFileName(name, lastModified); // NOI18N
                File backupCopy = new File(backupName);
                try {
                FileUtil.copy(file, backupCopy);
                log.debug("created new switch list backup file {}", backupName);
                } catch (Exception e) {
                    log.error("could not create switch list backup file {}", backupName);
                }
            }
        }
    }
    
    /**
     * Save previous train build status file in a separate directory called
     * BuildStatusBackups. Each build status is saved in a unique directory using
     * the train's name. 
     * @param name train's name
     */
    public void savePreviousBuildStatusFile(String name) {
        if (Setup.isSaveTrainManifestsEnabled()) {
            // create the build status backup directory
            createFile(getBackupBuildStatusDirectoryName() + " ", false); // no backup
            // now create unique backup directory for each train
            createFile(getBackupBuildStatusDirectoryName(name) + " ", false); // no backup
            // get old build status file for this train
            File file = findFile(defaultBuildReportFileName(name));
            if (file == null) {
                log.debug("No ({}) train build status file to backup", name);
            } else if (file.canRead()) {
                String lastModified = new SimpleDateFormat("yyyyMMdd-HHmmss").format(file.lastModified()); // NOI18N
                String backupName = getBackupBuildStatusFileName(name, lastModified); // NOI18N
                File backupCopy = new File(backupName);
                try {
                FileUtil.copy(file, backupCopy);
                log.debug("created new train build status backup file {}", backupName);
                } catch (Exception e) {
                    log.error("could not create train build status backup file {}", backupName);
                }
            }
        }
    }

    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(TrainManagerXml.class);

    @Override
    public void initialize() {
        load();
    }

}
