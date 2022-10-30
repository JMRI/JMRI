package jmri.jmrix.can.cbus.eventtable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import java.nio.file.*;

import javax.annotation.Nonnull;

import jmri.jmrit.XmlFile;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to provide access to the EventTableData.xml file.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableXmlFile extends XmlFile {

    final CanSystemConnectionMemo memo;

    public CbusEventTableXmlFile(@Nonnull CanSystemConnectionMemo memo){
        this.memo = memo;
    }

    /**
     * Get the full filename, including directory.
     * @return String of file path.
     */
    public String getDefaultFileName() {
        return getFileLocation() + File.separator + getFileName();
    }

    /**
     * Get the Event Table Filename, no directory.
     * @return just the filename.
     */
    public String getFileName() {
        return "EventTableData.xml";  // NOI18N
    }

    /**
     * Get the XML File for this connection.
     * Migrates from previous single instance Event Table.
     * @param store true to create new File if File does not exist.
     * @return File, or if store is false and File not found, returns null.
     */
    public File getFile(boolean store) {
        // Verify that directory:cbus exists
        FileUtil.createDirectory(getFileLocation());
        migrateFileLocation();

        File file = findFile(getDefaultFileName());
        if (file == null && store) {
            file = new File(getDefaultFileName());
        }
        return file;
    }

    /**
     * Absolute path to directory of Event Table file.
     * No trailing file separator.
     *
     * @return path to location
     */
    public String getFileLocation() {
        return FileUtil.getUserFilesPath() + "cbus" + File.separator + memo.getSystemPrefix() ;  // NOI18N
    }

    protected final String oldFileLocation = FileUtil.getUserFilesPath() + "cbus" ;

    private void migrateFileLocation(){
        if ( findFile(oldFileLocation + File.separator + getFileName()) == null ){
            return;
        }
        try {
            migrate(Paths.get(oldFileLocation), getFileLocation(), memo.getSystemPrefix() );
            log.warn("Migrated existing CBUS Event Table Data to {}", memo.getUserName());
        } catch(IOException e){
            log.error("Unable to migrate CBUS Data ",e);
        }
    }

    // also used in CbusNodeBackupFile for migrating to multi-system file support.
    public static void migrate(Path fromPath, String newLocation,
        String systemPrefix) throws IOException {

        String oldCbusDirectory = File.separator + "cbus" + File.separator;
        String newCbusDirectory = oldCbusDirectory + systemPrefix + File.separator; 

        Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileString = file.toAbsolutePath().toString();
                if ( ! fileString.contains(newLocation) ) { // not in new directory
                    String newPathString = fileString.replace( oldCbusDirectory  , newCbusDirectory);
                    Files.copy(file, Paths.get(newPathString), StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventTableXmlFile.class);

}
