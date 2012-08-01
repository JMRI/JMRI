// Backup.java

// Will be deleted after the next commit.

package jmri.jmrit.operations.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.OperationsXml;

/**
 * Backs up Operations files. Creates the "backups" and date directories along
 * with the individual backup files under the Operations root directory.
 * 
 * Automatic backups go into a separate directory. Restructured to better
 * support both regular and automatic backups.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @author Gregory Madsen Copyright (C) 2012
 * @version $Revision$
 */
@Deprecated // July 13, 2012 (Version 3.1.1)
public class Backup extends XmlFile {
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(Backup.class.getName());

	// Not sure what this indicates, but it gets in the way of autosaving at
	// points in the program other than start-up, so disabled.
	// private static boolean saved = false;

	// The root directory for all Operations files, usually
	// "user / name / JMRI / operations"
	private File operationsRoot = null;

	// The full path to the root directory for automatic backups.
	// For testing, this can be set to something other than the normal value
	// that is set in the default constructor by using the alternate
	// constructor.
	private File autoBackupRoot = null;

	public File getAutoBackupRoot() {
		return autoBackupRoot;
	}

	// The full path to the root directory for regular backups.
	// For testing, this can be set to something other than the normal value
	// that is set in the default constructor by using the alternate
	// constructor.
	private File defaultBackupRoot = null;

	public File getDefaultBackupRoot() {
		return defaultBackupRoot;
	}

	// These constitute the set of files for a complete backup set.
	private String[] backupSetFileNames = new String[] { "Operations.xml",
			"OperationsCarRoster.xml", "OperationsEngineRoster.xml",
			"OperationsLocationRoster.xml", "OperationsRouteRoster.xml",
			"OperationsTrainRoster.xml" };

	public String[] getBackupSetFileNames() {
		return backupSetFileNames;
	}

	/**
	 * Creates a Backup instance and initializes the root directories for
	 * backups to their normal default values.
	 */
	public Backup() {
		// Create all of the default values here so that they are in one place.
		operationsRoot = new File(XmlFile.prefsDir(),
				OperationsXml.getOperationsDirectoryName());

		autoBackupRoot = new File(operationsRoot, "autoBackups");

		defaultBackupRoot = new File(operationsRoot, "backups");
	}

	/**
	 * Creates a Backup instance and initializes the root directories for
	 * backups from the given values. This constructor should only be used for
	 * testing purposes when a different directory structure is needed.
	 * 
	 * @param operationsRoot
	 * @param autoBackupRoot
	 * @param defaultBackupRoot
	 */
	public Backup(File operationsRoot, File autoBackupRoot,
			File defaultBackupRoot) {

		this.operationsRoot = operationsRoot;

		this.autoBackupRoot = autoBackupRoot;

		this.defaultBackupRoot = defaultBackupRoot;
	}

	/**
	 * Backs up Operations files to a generated directory under the automatic
	 * backup root directory.
	 * 
	 * @return
	 */
	public synchronized boolean autoBackup() {
		boolean result = false;

		// Not sure what this indicates, but it gets in the way of autosaving at
		// points in the program other than start-up.
		// if (!saved) {
		// CarManagerXml.instance(); // make sure all files have been loaded

		// Get a name for this backup set that does not already exist.
		String setName = suggestAutoBackupSetName();

		result = copyBackupSet(operationsRoot,
				new File(autoBackupRoot, setName));

		return result;
	}

	/**
	 * Backs up Operations files to the named directory under the default backup
	 * root directory.
	 * 
	 * @param setName
	 *            The name of the new backup set
	 * @return true if successful, false if not.
	 */
	public boolean backupFilesToDefault(String setName) {
		return copyBackupSet(operationsRoot, new File(defaultBackupRoot,
				setName));
	}

	/**
	 * Creates backup files for the directory specified. Assumes that
	 * backupDirectory is a fully qualified path where the individual files will
	 * be created. This will backup files to any directory that does not have to
	 * part of the JMRI hierarchy.
	 * 
	 * @param backupDirectory
	 *            The directory to use for the backup.
	 * @return true if successful.
	 */
	public boolean backupFilesTo(File backupDirectory) {
		return copyBackupSet(operationsRoot, backupDirectory);
	}

	/**
	 * Returns a list of the Backup Sets under the Automatic backup root.
	 * 
	 */
	public String[] getAutomaticBackupSetList() {
		String[] backupDirectoryNames = { "<Empty>" };
		try {
			File file = autoBackupRoot;
			if (!file.exists()) {
				log.error("Automatic backup directory does not exist");
				return backupDirectoryNames;
			}

			backupDirectoryNames = file.list();

		} catch (Exception e) {
			log.error("Exception while making automatic backup list, may not be complete: "
					+ e);
		}
		return backupDirectoryNames;
	}

	public File[] getAutomaticBackupDirs() {
		// Returns a list of File objects for the backup sets in the automatic
		// backup store.
		File[] sets = null;
		sets = autoBackupRoot.listFiles();
		return sets;
	}

	public BackupSet[] getAutomaticBackupSets() {
		// This is a bit of a kludge for now, until I learn more about dynamic
		// sets
		File[] dirs = autoBackupRoot.listFiles();
		BackupSet[] sets = new BackupSet[dirs.length];

		for (int i = 0; i < dirs.length; i++) {
			sets[i] = new BackupSet(dirs[i]);
		}

		return sets;
	}

	public BackupSet[] getBackupSets(BackupSetStore store) {
		// Gets a list from the appropriate backup store.
		// This is a bit of a kludge for now, until I learn more about dynamic
		// sets
		File[] dirs;
		switch (store) {
		case Automatic:
			dirs = autoBackupRoot.listFiles();
			break;

		case Default:
			dirs = defaultBackupRoot.listFiles();
			break;

		default:
			throw new NotImplementedException();
		}

		BackupSet[] sets = new BackupSet[dirs.length];

		for (int i = 0; i < dirs.length; i++) {
			sets[i] = new BackupSet(dirs[i]);
		}
		
		return sets;
	}

	/**
	 * Returns a list of the Backup Sets under the Default backup root.
	 * 
	 */
	public String[] getDefaultBackupSetList() {
		String[] backupDirectoryNames = { "<Empty>" };
		try {
			File file = defaultBackupRoot;
			if (!file.exists()) {
				log.error("Default backup directory does not exist");
				return backupDirectoryNames;
			}

			if (file.list().length > 0) {
				backupDirectoryNames = file.list();
			}

		} catch (Exception e) {
			log.error("Exception while making Default backup list, may not be complete: "
					+ e);
		}
		return backupDirectoryNames;
	}

	/**
	 * Check to see if the given backup set already exists in the Default
	 * backups
	 * 
	 * @param setName
	 * @return true if it exists
	 */
	public boolean checkIfDefaultBackupSetExists(String setName) {
		// This probably needs to be simplified, but leave for now.
		try {
			File file = new File(defaultBackupRoot, setName);

			if (file.exists())
				return true;
		} catch (Exception e) {
			log.error("Exception during directory exists check");
		}
		return false;
	}

	/**
	 * Restores a Backup Set with the given name from the Automatic backup
	 * store.
	 * 
	 * @param directoryName
	 * @return
	 */
	public boolean restoreFilesFromAutomatic(String directoryName) {
		return copyBackupSet(new File(autoBackupRoot, directoryName),
				operationsRoot);
	}

	/**
	 * Restores a Backup Set with the given name from the Default backup store.
	 * 
	 * @param directoryName
	 * @return
	 */
	public boolean restoreFilesFromDefault(String directoryName) {
		return copyBackupSet(new File(defaultBackupRoot, directoryName),
				operationsRoot);
	}

	/**
	 * Restores a Backup Set from the given directory.
	 * 
	 * @param directory
	 * @return true if successful, false if not.
	 */
	public boolean restoreFilesFrom(File directory) {
		log.debug("restoring files from directory "
				+ directory.getAbsolutePath());

		return copyBackupSet(directory, operationsRoot);
	}

	/**
	 * Copies a complete set of Operations files from one directory to another
	 * directory. Usually used to copy to or from a backup location. Creates the
	 * destination directory if it does not exist.
	 * 
	 * This should probably be improved to copy specific named files rather than
	 * just all .XML files.
	 * 
	 * @param directory
	 * @return true if successful, false if not.
	 */
	public boolean copyBackupSet(File sourceDir, File destDir) {
		// Adapted from the original restore method.
		log.debug("copying backup set from: " + sourceDir + " to: " + destDir);

		try {
			if (!sourceDir.exists())
				// This should probably throw an exception, as the dir should
				// exist.
				return false;

			// Ensure destination directory exists
			destDir.mkdirs();

			// This should probably use a defined list of names.
			String[] operationFileNames = sourceDir.list();

			// check for at least 6 operation files
			if (operationFileNames.length < 6) {
				log.error("Only " + operationFileNames.length
						+ " files found in directory "
						+ sourceDir.getAbsolutePath());
				return false;
			}

			// TODO check for the correct operation file names
			int fileCount = 0;
			String fileName = null;

			for (int i = 0; i < operationFileNames.length; i++) {
				fileName = operationFileNames[i];

				// skip non-xml files
				if (!fileName.toUpperCase().endsWith(".XML"))
					continue;

				log.debug("copying file: " + fileName);

				fileCount++;

				File src = new File(sourceDir, fileName);
				File dst = new File(destDir, fileName);

				FileHelper.copy(src.getAbsolutePath(), dst.getAbsolutePath(),
						true);
			}
			if (fileCount < 6)
				return false;

			return true;
		} catch (Exception e) {
			log.error("Exception while copying backup set files, may not be complete: "
					+ e);
			return false;
		}
	}

	/**
	 * Reloads the demo Operations files that are distributed with JMRI.
	 */
	public boolean loadDemoFiles() {
		return copyBackupSet(new File(XmlFile.xmlDir(), "demoOperations"),
				operationsRoot);
	}

	/**
	 * Helper method to search for an available directory name under the Default
	 * backup root.
	 * 
	 * @return A backup set name that is not already in use.
	 */
	public String suggestDefaultBackupSetName() {
		return suggestBackupSetNameFor(defaultBackupRoot);
	}

	/**
	 * Helper method to search for an available directory name under the
	 * Automatic backup root.
	 * 
	 * @return A backup set name that is not already in use.
	 */
	public String suggestAutoBackupSetName() {
		return suggestBackupSetNameFor(autoBackupRoot);
	}

	/**
	 * Searches for an unused directory name, based on the default base name,
	 * under the given directory. A name suffix as appended to the base name and
	 * can range from 00 to 99.
	 * 
	 * @param backupRoot
	 * @return A backup set name that is not already in use.
	 */
	private String suggestBackupSetNameFor(File backupRoot) {
		// Start with a base name that is derived from today's date
		// This checks to see if the default name already exists under the given
		// backup root directory.
		// If it exists, the name is incremented by 1 up to 99 and checked
		// again.
		String baseName = getDate();
		String fullName = null;

		// Check for up to 100 backup file names to see if they already exist
		for (int i = 0; i < 99; i++) {
			// Create the trial name, then see if it already exists.
			fullName = String.format("%s_%02d", baseName, i);

			File testPath = new File(backupRoot, fullName);

			if (!testPath.exists()) {
				return fullName; // Found an unused name
			}

			// Otherwise complain and keep trying...
			log.debug("Operations backup directory: " + testPath
					+ " already exists");
		}

		// If we get here, we have tried all 100 variants without success. This
		// should probably throw an exception, but for now it just returns the
		// last file name tried.
		return fullName;
	}

	/**
	 * Reset Operations by deleting xml files, leaves directories and backup
	 * files in place.
	 */
	public void reset() {
		// Maybe this should also only delete specific files used by Operations,
		// and not just all XML files.
		File files = operationsRoot;

		if (!files.exists())
			return;

		String[] operationFileNames = files.list();
		for (int i = 0; i < operationFileNames.length; i++) {
			// skip non-xml files
			if (!operationFileNames[i].toUpperCase().endsWith(".XML"))
				continue;
			//
			log.debug("deleting file: " + operationFileNames[i]);
			File file = new File(operationsRoot + File.separator
					+ operationFileNames[i]);
			if (!file.delete())
				log.debug("file not deleted");
		}
	}

	/**
	 * Returns the current date formatted for use as part of a Backup Set name.
	 */
	private String getDate() {
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH) + 1;
		String m = Integer.toString(month);
		if (month < 10) {
			m = "0" + Integer.toString(month);
		}
		int day = now.get(Calendar.DATE);
		String d = Integer.toString(day);
		if (day < 10) {
			d = "0" + Integer.toString(day);
		}
		String date = "" + now.get(Calendar.YEAR) + "_" + m + "_" + d;
		return date;
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

//		/**
//		 * Copies an existing file to a new file. Overwriting a file of the same
//		 * name is not allowed. The destination directory must exist.
//		 * 
//		 * @param sourceFileName
//		 * @param destFileName
//		 * @throws IOException
//		 */
//		public static void copy(String sourceFileName, String destFileName)
//				throws IOException {
//			copy(sourceFileName, destFileName, false);
//		}

		/**
		 * Copies an existing file to a new file. Overwriting a file of the same
		 * name is allowed. The destination directory must exist.
		 * 
		 * @param sourceFileName
		 * @param destFileName
		 * @param overwrite
		 * @throws IOException
		 */
		public static void copy(String sourceFileName, String destFileName,
				Boolean overwrite) throws IOException {

			// IF we can't overwrite the destination, check if the destination
			// already exists
			if (!overwrite) {
				if (new File(destFileName).exists()) {
					throw new IOException(
							"Destination file exists and overwrite is false.");
				}
			}

			InputStream source = new FileInputStream(sourceFileName);
			OutputStream dest = new FileOutputStream(destFileName);

			byte[] buffer = new byte[1024];

			int len;

			while ((len = source.read(buffer)) > 0) {
				dest.write(buffer, 0, len);
			}

			source.close();
			dest.close();

			// Now update the last modified time to equal the source file.
			File src = new File(sourceFileName);
			File dst = new File(destFileName);

			dst.setLastModified(src.lastModified());
		}
	}

	public enum BackupSetStore {
		Automatic, Default

	}
}
