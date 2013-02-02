// AutoBackup.java

package jmri.jmrit.operations.setup;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;

/**
 * Specific Backup class for backing up and restoring Operations working files
 * to the Automatic Backup Store. Derived from BackupBase.
 * 
 * @author Gregory Madsen Copyright (C) 2012
 */
public class AutoBackup extends BackupBase {
	static Logger log = org.apache.log4j.Logger
			.getLogger(AutoBackup.class.getName());

	/**
	 * Creates an AutoBackup instance and initializes the root directory
	 * to the given name.
	 */
	public AutoBackup() {
		super("autoBackups"); // NOI18N
	}

	/**
	 * Backs up Operations files to a generated directory under the automatic
	 * backup root directory.
	 * 
	 * @throws Exception
	 */
	public synchronized void autoBackup() throws IOException {

		// Get a name for this backup set that does not already exist.
		String setName = suggestBackupSetName();

		copyBackupSet(_operationsRoot, new File(_backupRoot, setName));
	}
}
