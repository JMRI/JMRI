package jmri.jmrit.operations.setup;


/**
 * Specific Backup class for backing up and restoring Operations working files
 * to the Default Backup Store. Derived from BackupBase.
 *
 * @author Gregory Madsen Copyright (C) 2012
 */
public class DefaultBackup extends BackupBase {

//    private final static Logger log = LoggerFactory.getLogger(DefaultBackup.class);

    /**
     * Creates a DefaultBackup instance and initializes the root directory to
     * the given name.
     */
    public DefaultBackup() {
        super("backups"); // NOI18N
    }
}
