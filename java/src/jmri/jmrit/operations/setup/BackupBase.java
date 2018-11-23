package jmri.jmrit.operations.setup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.OperationsXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for backing up and restoring Operations working files. Derived
 * classes implement specifics for working with different backup set stores,
 * such as Automatic and Default backups.
 *
 * @author Gregory Madsen Copyright (C) 2012
 */
public abstract class BackupBase {

    private final static Logger log = LoggerFactory.getLogger(BackupBase.class);

    // Just for testing......
    // If this is not null, it will be thrown to simulate various IO exceptions
    // that are hard to reproduce when running tests..
    public RuntimeException testException = null;

    // The root directory for all Operations files, usually
    // "user / name / JMRI / operations"
    protected File _operationsRoot = null;

    public File getOperationsRoot() {
        return _operationsRoot;
    }

    // This will be set to the appropriate backup root directory from the
    // derived
    // classes, as their constructor will fill in the correct directory.
    protected File _backupRoot;

    public File getBackupRoot() {
        return _backupRoot;
    }

    // These constitute the set of files for a complete backup set.
    private final String[] _backupSetFileNames = new String[]{"Operations.xml", // NOI18N
            "OperationsCarRoster.xml", "OperationsEngineRoster.xml", // NOI18N
            "OperationsLocationRoster.xml", "OperationsRouteRoster.xml", // NOI18N
            "OperationsTrainRoster.xml"}; // NOI18N

    private final String _demoPanelFileName = "Operations Demo Panel.xml"; // NOI18N

    public String[] getBackupSetFileNames() {
        return _backupSetFileNames.clone();
    }

    /**
     * Creates a BackupBase instance and initializes the Operations root
     * directory to its normal value.
     * @param rootName Directory name to use.
     */
    protected BackupBase(String rootName) {
        // A root directory name for the backups must be supplied, which will be
        // from the derived class constructors.
        if (rootName == null) {
            throw new IllegalArgumentException("Backup root name can't be null"); // NOI18N
        }
        _operationsRoot = new File(OperationsXml.getFileLocation(), OperationsXml.getOperationsDirectoryName());

        _backupRoot = new File(getOperationsRoot(), rootName);

        // Make sure it exists
        if (!getBackupRoot().exists()) {
            Boolean ok = getBackupRoot().mkdirs();
            if (!ok) {
                throw new RuntimeException("Unable to make directory: " // NOI18N
                        + getBackupRoot().getAbsolutePath());
            }
        }

        // We maybe want to check if it failed and throw an exception.
    }

    /**
     * Backs up Operations files to the named backup set under the backup root
     * directory.
     *
     * @param setName The name of the new backup set
     * @throws java.io.IOException Due to trouble writing files
     */
    public void backupFilesToSetName(String setName) throws IOException {
        validateNotNullOrEmpty(setName);

        copyBackupSet(getOperationsRoot(), new File(getBackupRoot(), setName));
    }

    private void validateNotNullOrEmpty(String s) {
        if (s == null || s.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "string cannot be null or empty."); // NOI18N
        }

    }

    /**
     * Creates backup files for the directory specified. Assumes that
     * backupDirectory is a fully qualified path where the individual files will
     * be created. This will backup files to any directory which does not have
     * to be part of the JMRI hierarchy.
     *
     * @param backupDirectory The directory to use for the backup.
     * @throws java.io.IOException Due to trouble writing files
     */
    public void backupFilesToDirectory(File backupDirectory) throws IOException {
        copyBackupSet(getOperationsRoot(), backupDirectory);
    }

    /**
     * Returns a sorted list of the Backup Sets under the backup root.
     * @return A sorted backup list.
     *
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "not possible")  // NOI18N
    public String[] getBackupSetList() {
        String[] setList = getBackupRoot().list();
        // no guarantee of order, so we need to sort
        java.util.Arrays.sort(setList);
        return setList;
    }

    public File[] getBackupSetDirs() {
        // Returns a list of File objects for the backup sets in the
        // backup store.
        // Not used at the moment, and can probably be removed in favor of
        // getBackupSets()
        File[] dirs = getBackupRoot().listFiles();

        return dirs;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "not possible")  // NOI18N
    public BackupSet[] getBackupSets() {
        // This is a bit of a kludge for now, until I learn more about dynamic
        // sets
        File[] dirs = getBackupRoot().listFiles();
        Arrays.sort(dirs);
        BackupSet[] sets = new BackupSet[dirs.length];

        for (int i = 0; i < dirs.length; i++) {
            sets[i] = new BackupSet(dirs[i]);
        }

        return sets;
    }

    /**
     * Check to see if the given backup set already exists in the backup store.
     * @param setName The directory name to check.
     *
     * @return true if it exists
     */
    public boolean checkIfBackupSetExists(String setName) {
        // This probably needs to be simplified, but leave for now.
        validateNotNullOrEmpty(setName);

        try {
            File file = new File(getBackupRoot(), setName);

            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            log.error("Exception during backup set directory exists check");
        }
        return false;
    }

    /**
     * Restores a Backup Set with the given name from the backup store.
     * @param setName The directory name.
     *
     * @throws java.io.IOException Due to trouble loading files
     */
    public void restoreFilesFromSetName(String setName) throws IOException {
        copyBackupSet(new File(getBackupRoot(), setName), getOperationsRoot());
    }

    /**
     * Restores a Backup Set from the given directory.
     * @param directory The File directory.
     *
     * @throws java.io.IOException Due to trouble loading files
     */
    public void restoreFilesFromDirectory(File directory) throws IOException {
        log.debug("restoring files from directory {}", directory.getAbsolutePath());

        copyBackupSet(directory, getOperationsRoot());
    }

    /**
     * Copies a complete set of Operations files from one directory to another
     * directory. Usually used to copy to or from a backup location. Creates the
     * destination directory if it does not exist.
     *
     * Only copies files that are included in the list of Operations files.
     * @param sourceDir From Directory
     * @param destDir To Directory
     *
     * @throws java.io.IOException Due to trouble reading or writing
     */
    public void copyBackupSet(File sourceDir, File destDir) throws IOException {
        log.debug("copying backup set from: {} to: {}", sourceDir, destDir);
        log.info("Saving copy of operations files to: {}", destDir);

        if (!sourceDir.exists()) // This throws an exception, as the dir should
        // exist.
        {
            throw new IOException("Backup Set source directory: " // NOI18N
                    + sourceDir.getAbsolutePath() + " does not exist"); // NOI18N
        }
        // See how many Operations files we have. If they are all there, carry
        // on, if there are none, just return, any other number MAY be an error,
        // so just log it.
        // We can't throw an exception, as this CAN be a valid state.
        // There is no way to tell if a missing file is an error or not the way
        // the files are created.

        int sourceCount = getSourceFileCount(sourceDir);

        if (sourceCount == 0) {
            log.debug("No source files found in {} so skipping copy.", sourceDir.getAbsolutePath()); // NOI18N
            return;
        }

        if (sourceCount != _backupSetFileNames.length) {
            log.warn("Only {} file(s) found in directory {}", sourceCount, sourceDir.getAbsolutePath());
            // throw new IOException("Only " + sourceCount
            // + " file(s) found in directory "
            // + sourceDir.getAbsolutePath());
        }

        // Ensure destination directory exists
        if (!destDir.exists()) {
            // Note that mkdirs does NOT throw an exception on error.
            // It will return false if the directory already exists.
            boolean result = destDir.mkdirs();

            if (!result) {
                // This needs to use a better Exception class.....
                throw new IOException(
                        destDir.getAbsolutePath() + " (Could not create all or part of the Backup Set path)"); // NOI18N
            }
        }

        // Just copy the specific Operations files, now that we know they are
        // all there.
        for (String name : _backupSetFileNames) {
            log.debug("copying file: {}", name);

            File src = new File(sourceDir, name);

            if (src.exists()) {
                File dst = new File(destDir, name);

                FileHelper.copy(src.getAbsolutePath(), dst.getAbsolutePath(), true);
            } else {
                log.debug("Source file: {} does not exist, and is not copied.", src.getAbsolutePath());
            }

        }

        // Throw a test exception, if we have one.
        if (testException != null) {
            testException.fillInStackTrace();
            throw testException;
        }
    }

    /**
     * Checks to see how many of the Operations files are present in the source
     * directory.
     * @param sourceDir The Directory to check.
     *
     * @return number of files
     */
    public int getSourceFileCount(File sourceDir) {
        int count = 0;
        Boolean exists;

        for (String name : _backupSetFileNames) {
            exists = new File(sourceDir, name).exists();
            if (exists) {
                count++;
            }
        }

        return count;
    }

    /**
     * Reloads the demo Operations files that are distributed with JMRI.
     *
     * @throws java.io.IOException Due to trouble loading files
     */
    public void loadDemoFiles() throws IOException {
        File fromDir = new File(XmlFile.xmlDir(), "demoOperations"); // NOI18N
        copyBackupSet(fromDir, getOperationsRoot());

        // and the demo panel file
        log.debug("copying file: {}", _demoPanelFileName);

        File src = new File(fromDir, _demoPanelFileName);
        File dst = new File(getOperationsRoot(), _demoPanelFileName);

        FileHelper.copy(src.getAbsolutePath(), dst.getAbsolutePath(), true);

    }

    /**
     * Searches for an unused directory name, based on the default base name,
     * under the given directory. A name suffix as appended to the base name and
     * can range from 00 to 99.
     *
     * @return A backup set name that is not already in use.
     */
    public String suggestBackupSetName() {
        // Start with a base name that is derived from today's date
        // This checks to see if the default name already exists under the given
        // backup root directory.
        // If it exists, the name is incremented by 1 up to 99 and checked
        // again.
        String baseName = getDate();
        String fullName = null;
        String[] dirNames = getBackupRoot().list();

        // Check for up to 100 backup file names to see if they already exist
        for (int i = 0; i < 99; i++) {
            // Create the trial name, then see if it already exists.
            fullName = String.format("%s_%02d", baseName, i); // NOI18N

            boolean foundFileNameMatch = false;
            for (String name : dirNames) {
                if (name.equals(fullName)) {
                    foundFileNameMatch = true;
                    break;
                }
            }
            if (!foundFileNameMatch) {
                return fullName;
            }

            //   This should also work, commented out by D. Boudreau
            //   The Linux problem turned out to be related to the order
            //   files names are returned by list().
            //   File testPath = new File(_backupRoot, fullName);
            //
            //   if (!testPath.exists()) {
            //    return fullName; // Found an unused name
            // Otherwise complain and keep trying...
            log.debug("Operations backup directory: {} already exists", fullName); // NOI18N
        }

        // If we get here, we have tried all 100 variants without success. This
        // should probably throw an exception, but for now it just returns the
        // last file name tried.
        return fullName;
    }

    /**
     * Reset Operations by deleting XML files, leaves directories and backup
     * files in place.
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "not possible")  // NOI18N
    public void deleteOperationsFiles() {
        // TODO Maybe this should also only delete specific files used by Operations,
        // and not just all XML files.
        File files = getOperationsRoot();

        if (!files.exists()) {
            return;
        }

        String[] operationFileNames = files.list();
        for (String fileName : operationFileNames) {
            // skip non-xml files
            if (!fileName.toUpperCase().endsWith(".XML")) // NOI18N
            {
                continue;
            }
            //
            log.debug("deleting file: {}", fileName);
            File file = new File(getOperationsRoot() + File.separator + fileName);
            if (!file.delete()) {
                log.debug("file not deleted");
            }
            // TODO This should probably throw an exception if a delete fails.
        }
    }

    /**
     * Returns the current date formatted for use as part of a Backup Set name.
     */
    private String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");  // NOI18N
        return simpleDateFormat.format(date);
    }

    /**
     * Helper class for working with Files and Paths. Should probably be moved
     * into its own public class.
     *
     * Probably won't be needed now that I discovered the File class and it can
     * glue together paths. Need to explore it a bit more.
     *
     * @author Gregory Madsen Copyright (C) 2012
     *
     */
    private static class FileHelper {

        /**
         * Copies an existing file to a new file. Overwriting a file of the same
         * name is allowed. The destination directory must exist.
         * @param sourceFileName From directory name
         * @param destFileName To directory name
         * @param overwrite When true overwrite any existing files
         * @throws IOException Thrown when overwrite false and destination directory exists.
         *
         */
        @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION")
        public static void copy(String sourceFileName, String destFileName,
                Boolean overwrite) throws IOException {

            // If we can't overwrite the destination, check if the destination
            // already exists
            if (!overwrite) {
                if (new File(destFileName).exists()) {
                    throw new IOException(
                            "Destination file exists and overwrite is false."); // NOI18N
                }
            }

            try (InputStream source = new FileInputStream(sourceFileName);
                    OutputStream dest = new FileOutputStream(destFileName)) {

                byte[] buffer = new byte[1024];

                int len;

                while ((len = source.read(buffer)) > 0) {
                    dest.write(buffer, 0, len);
                }
            } catch (IOException ex) {
                String msg = String.format("Error copying file: %s to: %s", // NOI18N
                        sourceFileName, destFileName);
                throw new IOException(msg, ex);
            }

            // Now update the last modified time to equal the source file.
            File src = new File(sourceFileName);
            File dst = new File(destFileName);

            Boolean ok = dst.setLastModified(src.lastModified());
            if (!ok) {
                throw new RuntimeException(
                        "Failed to set modified time on file: " // NOI18N
                                + dst.getAbsolutePath());
            }
        }
    }

}
