package jmri.jmrit.operations.setup;

import java.io.File;
import java.io.IOException;

/**
 * Specific Backup class for backing up and restoring Operations working files
 * to the Automatic Backup Store. Derived from BackupBase.
 *
 * @author Gregory Madsen Copyright (C) 2012
 */
public class AutoBackup extends BackupBase {

//    private final static Logger log = LoggerFactory.getLogger(AutoBackup.class);

    /**
     * Creates an AutoBackup instance and initializes the root directory to the
     * given name.
     */
    public AutoBackup() {
        super("autoBackups"); // NOI18N
    }

    /**
     * Backs up Operations files to a generated directory under the automatic
     * backup root directory.
     *
     * @throws java.io.IOException Due to trouble accessing files
     */
    public synchronized void autoBackup() throws IOException {

        // Get a name for this backup set that does not already exist.
        String setName = suggestBackupSetName();

        copyBackupSet(getOperationsRoot(), new File(getBackupRoot(), setName));
    }
}
