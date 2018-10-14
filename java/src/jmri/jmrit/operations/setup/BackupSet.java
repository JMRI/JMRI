/**
 *
 */
package jmri.jmrit.operations.setup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.util.Date;

/**
 * Represents the set of Operations files that is considered a "Backup" of the
 * current Operations files.
 *
 * It can facilitate the display and selection of backup sets using a GUI.
 *
 * This class needs tests.......
 *
 * @author Gregory Madsen Copyright (C) 2012
 *
 */
public class BackupSet {

    private String _setName;

    public String getSetName() {
        return _setName;
    }

    private Date _lastModifiedDate;
    private File _dir;

    public BackupSet(File dir) {
        _dir = dir;
        _setName = dir.getName();
        _lastModifiedDate = new Date(dir.lastModified());
    }

    public void delete() {
        deleteDirectoryAndFiles(_dir);
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "not possible")
    private void deleteDirectoryAndFiles(File dir) {
        // Deletes all of the files in a directory, and then the directory
        // itself.
        // This is NOT a general purpose method, as it only handles directories
        // with only files and no sub directories.
        // This probably needs to handle failures. delete() returns false if it fails.
        Boolean ok;
        for (File f : dir.listFiles()) {
            // Delete files first
            if (f.isFile()) {
                ok = f.delete();
                if (!ok) {
                    throw new RuntimeException("Failed to delete file: " + f.getAbsolutePath()); // NOI18N
                }
            }
        }

        ok = dir.delete();
        if (!ok) {
            throw new RuntimeException("Failed to delete directory: " + dir.getAbsolutePath()); // NOI18N
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", _setName, _lastModifiedDate); // NOI18N
    }
}
