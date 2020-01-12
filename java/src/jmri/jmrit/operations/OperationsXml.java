package jmri.jmrit.operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.FileUtil;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and stores the operation setup using xml files.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public abstract class OperationsXml extends XmlFile {

    /**
     * Store the all of the operation train objects in the default place,
     * including making a backup if needed
     */
    public void writeOperationsFile() {
        createFile(getDefaultOperationsFilename(), true); // make backup
        try {
            writeFile(getDefaultOperationsFilename());
        } catch (IOException e) {
            log.error("Exception while writing operation file, may not be complete: {}", e.getMessage());
        }
    }

    protected void load() {
        try {
            readFile(getDefaultOperationsFilename());
        } catch (IOException | JDOMException e) {
            log.error("Exception during operations file reading", e);
        }
    }

    protected File createFile(String fullPathName, boolean backupFile) {
        if (backupFile) {
            makeBackupFile(fullPathName);
        }
        File file = null;
        try {
            if (!checkFile(fullPathName)) {
                // log.debug("File "+fullPathName+ " does not exist, creating it");
                // The file does not exist, create it before writing
                file = new File(fullPathName);
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    if (!parentDir.mkdir()) {
                        log.error("Directory wasn't created");
                    }
                }
                if (file.createNewFile()) {
                    log.debug("File created {}", fullPathName);
                }
            } else {
                file = new File(fullPathName);
            }
        } catch (IOException e) {
            log.error("Exception while creating operations file, may not be complete: {}", e.getMessage());
        }
        return file;
    }

    protected void writeFile(String filename) throws FileNotFoundException, IOException {
        log.error("writeFile not overridden");
    }

    /**
     * @param filename The string file name.
     * @throws org.jdom2.JDOMException Due to XML parsing error
     * @throws java.io.IOException     Due to trouble accessing named file
     */
    abstract public void readFile(String filename) throws org.jdom2.JDOMException, java.io.IOException;

    private boolean dirty = false;

    public void setDirty(boolean b) {
        dirty = b;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void writeFileIfDirty() {
        if (isDirty()) {
            writeOperationsFile();
        }
    }

    public String getDefaultOperationsFilename() {
        return getFileLocation() + getOperationsDirectoryName() + File.separator + getOperationsFileName();
    }

    public static void setOperationsDirectoryName(String name) {
        operationsDirectoryName = name;
    }

    public static String getOperationsDirectoryName() {
        return operationsDirectoryName;
    }

    private static String operationsDirectoryName = "operations"; // NOI18N

    public void setOperationsFileName(String name) {
        operationsFileName = name;
    }

    public String getOperationsFileName() {
        return operationsFileName;
    }

    private String operationsFileName = "DefaultOperations.xml"; // should be overridden // NOI18N

    /**
     * Absolute path to location of Operations files.
     * <p>
     * Default is in the user's files path, but can be set to anything.
     *
     * @return The string path name.
     *
     * @see jmri.util.FileUtil#getUserFilesPath()
     */
    public static String getFileLocation() {
        return fileLocation;
    }

    /**
     * Set path to location of Operations files.
     * <p>
     * Default is in the user's files path, but can be set to anything.
     *
     * @param location path to file, including trailing file separator.
     */
    public static void setFileLocation(String location) {
        fileLocation = location;
    }

    private static String fileLocation = FileUtil.getUserFilesPath();

    /**
     * Convert standard string to xml string one character at a time expect when
     * a \n is found. In that case, insert a {@literal "<?p?>"}.
     *
     * @param comment standard string
     * @return string converted to xml format.
     */
    @Deprecated
    public static String convertToXmlComment(String comment) {
        StringBuilder buf = new StringBuilder();
        for (int k = 0; k < comment.length(); k++) {
            if (comment.startsWith("\n", k)) { // NOI18N
                buf.append("<?p?>"); // NOI18N
            } else {
                buf.append(comment.substring(k, k + 1));
            }
        }
        return buf.toString();
    }

    /**
     * Convert xml string comment to standard string format one character at a
     * time, except when {@literal <?p?>} is found. In that case, insert a \n
     * and skip over those characters.
     *
     * @param comment input xml comment string
     * @return output string converted to standard format
     */
    @Deprecated
    public static String convertFromXmlComment(String comment) {
        StringBuilder buf = new StringBuilder();
        for (int k = 0; k < comment.length(); k++) {
            if (comment.startsWith("<?p?>", k)) { // NOI18N
                buf.append("\n"); // NOI18N
                k = k + 4;
            } else {
                buf.append(comment.substring(k, k + 1));
            }
        }
        return buf.toString();
    }

    /**
     * Checks name for the file control characters:
     *
     * @param name The string to check for a valid file name.
     * @return true if name is okay, false if name contains a control character.
     */
    public static boolean checkFileName(String name) {
        if (name.contains(".") || name.contains("<") || name.contains(">") // NOI18N
                || name.contains(":") || name.contains("\"") || name.contains("\\") // NOI18N
                || name.contains("/") || name.contains("|") || name.contains("?") // NOI18N
                || name.contains("*")) { // NOI18N
            return false;
        }
        return true;
    }

    /**
     * Saves operation files that have been modified.
     */
    public static void save() {
        InstanceManager.getDefault(OperationsSetupXml.class).writeFileIfDirty();
        InstanceManager.getDefault(LocationManagerXml.class).writeFileIfDirty(); // Need to save "moves" for track location
        InstanceManager.getDefault(RouteManagerXml.class).writeFileIfDirty(); // Only if user used setX&Y
        InstanceManager.getDefault(CarManagerXml.class).writeFileIfDirty(); // save train assignments
        InstanceManager.getDefault(EngineManagerXml.class).writeFileIfDirty(); // save train assignments
        InstanceManager.getDefault(TrainManagerXml.class).writeFileIfDirty(); // save train changes
    }

    /**
     * Checks to see if any operations files are dirty
     *
     * @return True if any operations parameters have been modified.
     */
    public static boolean areFilesDirty() {
        return InstanceManager.getDefault(OperationsSetupXml.class).isDirty()
                || InstanceManager.getDefault(LocationManagerXml.class).isDirty()
                || InstanceManager.getDefault(RouteManagerXml.class).isDirty()
                || InstanceManager.getDefault(CarManagerXml.class).isDirty()
                || InstanceManager.getDefault(EngineManagerXml.class).isDirty()
                || InstanceManager.getDefault(TrainManagerXml.class).isDirty();
    }

    private final static Logger log = LoggerFactory.getLogger(OperationsXml.class);

}
