// BackupBase.java

package jmri.jmrit.operations.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.OperationsXml;

/**
 * Base class for backing up and restoring Operations working files. Derived
 * classes implement specifics for working with different backup set stores,
 * such as Automatic and Default backups.
 * 
 * @author Gregory Madsen Copyright (C) 2012
 */
public abstract class BackupBase {
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(BackupBase.class.getName());

	// Just for testing......
	// If this is not null, it will be thrown to simulate various IO exceptions
	// that are hard to reproduce when running tests..
	public RuntimeException testException = null;

	// The root directory for all Operations files, usually
	// "user / name / JMRI / operations"
	protected File _operationsRoot = null;

	// This will be set to the appropriate backup root directory from the
	// derived
	// classes, as their constructor will fill in the correct directory.
	protected File _backupRoot;

	public File getBackupRoot() {
		return _backupRoot;
	}

	// These constitute the set of files for a complete backup set.
	private String[] _backupSetFileNames = new String[] { "Operations.xml",
			"OperationsCarRoster.xml", "OperationsEngineRoster.xml",
			"OperationsLocationRoster.xml", "OperationsRouteRoster.xml",
			"OperationsTrainRoster.xml" };

	public String[] getBackupSetFileNames() {
		return _backupSetFileNames;
	}

	/**
	 * Creates a BackupBase instance and initializes the Operations root
	 * directory to its normal value.
	 */
	protected BackupBase(String rootName) {
		// A root directory name for the backups must be supplied, which will be
		// from the derived class constructors.
		if (rootName == null)
			throw new IllegalArgumentException("Backup root name can't be null");

		_operationsRoot = new File(XmlFile.prefsDir(),
				OperationsXml.getOperationsDirectoryName());

		_backupRoot = new File(_operationsRoot, rootName);

		// Make sure it exists
		if (!_backupRoot.exists())
			_backupRoot.mkdirs();

		// We maybe want to check if it failed and throw an exception.
	}

	/**
	 * Backs up Operations files to the named backup set under the backup root
	 * directory.
	 * 
	 * @param setName
	 *            The name of the new backup set
	 * @return true if successful, false if not.
	 * @throws Exception
	 */
	public void backupFilesToSetName(String setName) throws IOException {
		validateNotNullOrEmpty(setName);

		copyBackupSet(_operationsRoot, new File(_backupRoot, setName));
	}

	private void validateNotNullOrEmpty(String s) {
		if (s == null || s.trim().length() == 0) {
			throw new IllegalArgumentException(
					"string cannot be null or empty.");
		}

	}

	/**
	 * Creates backup files for the directory specified. Assumes that
	 * backupDirectory is a fully qualified path where the individual files will
	 * be created. This will backup files to any directory which does not have
	 * to be part of the JMRI hierarchy.
	 * 
	 * @param backupDirectory
	 *            The directory to use for the backup.
	 * @return true if successful.
	 * @throws Exception
	 */
	public void backupFilesToDirectory(File backupDirectory) throws IOException {
		copyBackupSet(_operationsRoot, backupDirectory);
	}

	/**
	 * Returns a list of the Backup Sets under the backup root.
	 * 
	 */
	public String[] getBackupSetList() {
		String[] setList = _backupRoot.list();

		return setList;
	}

	public File[] getBackupSetDirs() {
		// Returns a list of File objects for the backup sets in the
		// backup store.
		// Not used at the moment, and can probably be removed in favor of
		// getBackupSets()
		File[] dirs = _backupRoot.listFiles();

		return dirs;
	}

	public BackupSet[] getBackupSets() {
		// This is a bit of a kludge for now, until I learn more about dynamic
		// sets
		File[] dirs = _backupRoot.listFiles();
		BackupSet[] sets = new BackupSet[dirs.length];

		for (int i = 0; i < dirs.length; i++) {
			sets[i] = new BackupSet(dirs[i]);
		}

		return sets;
	}

	/**
	 * Check to see if the given backup set already exists in the backup store.
	 * 
	 * @param setName
	 * @return true if it exists
	 */
	public boolean checkIfBackupSetExists(String setName) {
		// This probably needs to be simplified, but leave for now.
		validateNotNullOrEmpty(setName);

		try {
			File file = new File(_backupRoot, setName);

			if (file.exists())
				return true;
		} catch (Exception e) {
			log.error("Exception during backup set directory exists check");
		}
		return false;
	}

	/**
	 * Restores a Backup Set with the given name from the backup store.
	 * 
	 * @param directoryName
	 * @return
	 * @throws Exception
	 */
	public void restoreFilesFromSetName(String setName) throws IOException {
		copyBackupSet(new File(_backupRoot, setName), _operationsRoot);
	}

	/**
	 * Restores a Backup Set from the given directory.
	 * 
	 * @param directory
	 * @return true if successful, false if not.
	 * @throws Exception
	 */
	public void restoreFilesFromDirectory(File directory) throws IOException {
		log.debug("restoring files from directory "
				+ directory.getAbsolutePath());

		copyBackupSet(directory, _operationsRoot);
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
	 * @throws IOException
	 * @throws SetupException
	 */
	public void copyBackupSet(File sourceDir, File destDir) throws IOException {
		log.debug("copying backup set from: " + sourceDir + " to: " + destDir);

		if (!sourceDir.exists())
			// This throws an exception, as the dir should
			// exist.
			throw new IOException("Backup Set source directory: "
					+ sourceDir.getAbsolutePath() + " does not exist");

		// This should probably use a defined list of names.
		String[] operationFileNames = sourceDir.list();

		// check for at least 6 operation files
		// This really should check that all expected files are present.
		if (operationFileNames.length < 6) {
			log.error("Only " + operationFileNames.length
					+ " files found in directory "
					+ sourceDir.getAbsolutePath());
			throw new IOException("Only " + operationFileNames.length
					+ " files found in directory "
					+ sourceDir.getAbsolutePath());
		}

		// Ensure destination directory exists
		if (!destDir.exists()) {
			// Note that mkdirs does NOT throw an exception on error.
			// It will return false if the directory already exists.
			boolean result = destDir.mkdirs();

			if (!result) {
				// This needs to use a better Exception class.....
				throw new IOException(
						destDir.getAbsolutePath()
								+ " (Could not create all or part of the Backup Set path)");
			}
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

			FileHelper.copy(src.getAbsolutePath(), dst.getAbsolutePath(), true);
		}

		// This needs to be improved to only copy the specific Operations file
		// names.
		if (fileCount < 6)
			throw new IOException("The number of Files copied from "
					+ sourceDir.getAbsolutePath() + " was " + fileCount
					+ ", but should have been 6");

		// Throw the test exception, if we have one.
		if (testException != null) {
			testException.fillInStackTrace();
			throw testException;
		}
	}

	/**
	 * Reloads the demo Operations files that are distributed with JMRI.
	 * 
	 * @throws Exception
	 */
	public void loadDemoFiles() throws IOException {
		copyBackupSet(new File(XmlFile.xmlDir(), "demoOperations"),
				_operationsRoot);
	}

	/**
	 * Searches for an unused directory name, based on the default base name,
	 * under the given directory. A name suffix as appended to the base name and
	 * can range from 00 to 99.
	 * 
	 * @param _backupRoot
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

		// Check for up to 100 backup file names to see if they already exist
		for (int i = 0; i < 99; i++) {
			// Create the trial name, then see if it already exists.
			fullName = String.format("%s_%02d", baseName, i);

			File testPath = new File(_backupRoot, fullName);

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
	 * Reset Operations by deleting XML files, leaves directories and backup
	 * files in place.
	 */
	public void deleteOperationsFiles() {
		// Maybe this should also only delete specific files used by Operations,
		// and not just all XML files.
		File files = _operationsRoot;

		if (!files.exists())
			return;

		String[] operationFileNames = files.list();
		for (int i = 0; i < operationFileNames.length; i++) {
			// skip non-xml files
			if (!operationFileNames[i].toUpperCase().endsWith(".XML"))
				continue;
			//
			log.debug("deleting file: " + operationFileNames[i]);
			File file = new File(_operationsRoot + File.separator
					+ operationFileNames[i]);
			if (!file.delete())
				log.debug("file not deleted");
			// This should probably throw an exception if a delete fails.
		}
	}

	/**
	 * Returns the current date formatted for use as part of a Backup Set name.
	 */
	private String getDate() {
		// This could use some clean-up.... but works OK for now
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

			// If we can't overwrite the destination, check if the destination
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

}
