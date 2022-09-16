package jmri.jmrix.can.cbus.node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrix.can.CanSystemConnectionMemo;

import jmri.util.FileUtil;

/**
 * Class to define location for a CbusNodeBackup File.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupFile extends XmlFile {

    final CanSystemConnectionMemo memo;

    public CbusNodeBackupFile( CanSystemConnectionMemo memo ) {
        this.memo = memo;
    }

    private CanSystemConnectionMemo getMemo() {
        return( memo != null ? memo : InstanceManager.getDefault(CanSystemConnectionMemo.class) );
    }

    /**
     * Get Backup FileName for a given Node Number.
     * Includes full directory path and filename.
     *
     * @param nodeNum Node Number
     * @return the Backup File location within user directory.
     */
    @Nonnull
    public String getDefaultFileName(int nodeNum) {
        return getFileLocation() + File.separator + getFileName(nodeNum);
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
        // Verify that cbus/M/node/ directory exists
        FileUtil.createDirectory(getFileLocation());
        migrateFileLocation();

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
    public String getFileLocation() {
        return FileUtil.getUserFilesPath() 
        + "cbus" + File.separator + getMemo().getSystemPrefix() + File.separator + "nodes";  // NOI18N
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

    protected final String oldFileLocation = FileUtil.getUserFilesPath() + "cbus" + File.separator + "nodes";

    private void migrateFileLocation(){
        if ( findFile(oldFileLocation ) == null ){
            return;
        }
        try {
            jmri.jmrix.can.cbus.eventtable.CbusEventTableXmlFile.migrate(Paths.get(oldFileLocation), getFileLocation(), getMemo().getSystemPrefix() );
            Files.delete(Paths.get(oldFileLocation));
            log.warn("Migrated existing CBUS Node Data to {}", getMemo().getUserName());
        } catch(IOException e){
            log.error("Unable to migrate CBUS Data ",e);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeBackupFile.class);
}
