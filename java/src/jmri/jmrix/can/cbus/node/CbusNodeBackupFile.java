package jmri.jmrix.can.cbus.node;

import java.io.File;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;

/**
 * Class to define location for a CbusNodeBackup File.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupFile extends XmlFile {
    
    /**
     * Get Backup FileName for a given Node Number.
     *
     * @param nodeNum Node Number
     * @return the Backup File location within user directory.
     */
    @Nonnull
    public String getDefaultFileName(int nodeNum) {
        return getFileLocation() + getFileName(nodeNum);
    }

    /**
     * Get Backup File for a given Node Number.
     *
     * @param nodeNum Node Number
     * @param store True to make a new file if does not exist
     * @return the Backup File
     */
    @CheckForNull
    public File getFile(int nodeNum, boolean store) {
        // Verify that cbus/node/ directory exists
        FileUtil.createDirectory(getFileLocation());

        File file = findFile(getDefaultFileName(nodeNum));
        if (file == null && store) {
            file = new File(getDefaultFileName(nodeNum));
        }
        return file;
    }

    /**
     * Get Backup FileName for a given Node Number.
     *
     * @param nodeNum Node Number
     * @return the Backup FileName
     */
    public String getFileName(int nodeNum) {
        return nodeNum + ".xml";  // NOI18N
    }

    /**
     * Path to location of files.
     *
     * @return path to location
     */
    public static String getFileLocation() {
        return FileUtil.getUserFilesPath() 
        + "cbus" + File.separator + "nodes" + File.separator;  // NOI18N
    }

    /**
     * Delete Backup File for a given Node Number.
     *
     * @param nodeNum Node Number
     * @return true if no file to delete, or delete success. Else false.
     */
    public boolean deleteFile(int nodeNum) {
        File toDelete = getFile(nodeNum,false);
        if (toDelete != null ) {
            return toDelete.delete();
        }
        return true;
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeXml.class);
}
