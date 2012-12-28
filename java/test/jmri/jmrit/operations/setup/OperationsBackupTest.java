//OperationsBackup2Test.java

package jmri.jmrit.operations.setup;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.OperationsXml;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Calendar;
import java.util.Locale;

/**
 * Tests for the new Operations Setup Backup classes used for copying and
 * restoring file backup sets. These test the AutoBackup and DefaultBackup
 * classes that are derived from BackupBase.
 * 
 * These tests use dummy XML files for the copying to make testing the outcomes
 * easier. It is assumed that there are other tests to verify that the correct
 * format XML files get created. This only tests the copying parts of the Backup
 * and Restore operations.
 * 
 * Tests include: - copying to / from regular backup location - copying to /
 * from automatic backup location
 * 
 * Source files are in Operations / JUnitTests.
 * 
 * Backup dirs are in: Operations / JUnitTests / backups and
 * 
 * Operations / JUnitTests / autoBackups
 * 
 * 
 * Still to do: - Need file comparison method to verify exactly that the files
 * are the same.
 * 
 * 
 * @author Gregory Madsen Copyright (C) 2012
 * @version $Revision: 00001 $
 */
public class OperationsBackupTest extends TestCase {

	private File operationsRoot;

	public File getOperationsRoot() {
		return operationsRoot;
	}

	private File defaultBackupRoot;

	public File getDefaultBackupRoot() {
		return defaultBackupRoot;
	}

	private File autoBackupRoot;

	public File getAutoBackupRoot() {
		return autoBackupRoot;
	}

	private String[] regularBackupSetFileNames;

	// public String[] getRegularBackupSetFileNames() {
	// return regularBackupSetFileNames;
	// }

	// private String[] testBackupSetFileNames;
	//
	// public String[] getTestBackupSetFileNames() {
	// return testBackupSetFileNames;
	// }

	public OperationsBackupTest(String s) {
		super(s);

		// Set the static Operations root directory used for tests
		OperationsXml.setOperationsDirectoryName("operations" + File.separator
				+ "JUnitTest");

		// Initialize our root directories as they should be so that we can
		// verify the Backup classes use the correct locations.
		//
		// Correct structure is:
		// <user>/JMRI/Operations / JUnitTest / backups
		// <user>/JMRI/Operations / JUnitTest / autoBackups

		operationsRoot = new File(XmlFile.prefsDir(),
				OperationsXml.getOperationsDirectoryName());

		autoBackupRoot = new File(operationsRoot, "autoBackups");

		defaultBackupRoot = new File(operationsRoot, "backups");

		// Build the list of test file names from the regular list.
		// We assume this works, as there is a specific test for it later.
		BackupBase backup = new DefaultBackup();
		regularBackupSetFileNames = backup.getBackupSetFileNames();

		// testBackupSetFileNames = new
		// String[regularBackupSetFileNames.length];
		//
		// for (int i = 0; i < regularBackupSetFileNames.length; i++) {
		// testBackupSetFileNames[i] = "NEW_TEST_"
		// + regularBackupSetFileNames[i];
		// }
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = { "-noloading",
				OperationsBackupTest.class.getName() };
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsBackupTest.class);
		return suite;
	}

	/**
	 * Test-by test initialization.
	 */
	@Override
	protected void setUp() throws IOException {
		apps.tests.Log4JFixture.setUp();

		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);

		// Delete any existing auto or default backup sets
		if (autoBackupRoot.exists()) {
			for (File f : autoBackupRoot.listFiles()) {
				// Delete directories assuming they are backup sets
				if (f.isDirectory()) {
					deleteDirectoryAndFiles(f);
				}
			}
		}

		if (defaultBackupRoot.exists()) {
			for (File f : defaultBackupRoot.listFiles()) {
				// Delete directories assuming they are backup sets
				if (f.isDirectory()) {
					deleteDirectoryAndFiles(f);
				}
			}
		}

		// Make sure we have a fresh set of file before each test.
		// Clean up our test files, just to be safe.
		deleteTestFiles();
		createTestFiles();
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() {
		// restore locale
		Locale.setDefault(Locale.getDefault());
		apps.tests.Log4JFixture.tearDown();
		deleteTestFiles();
	}

	// Some private helper methods......
	public void createTestFiles() throws IOException {
		// Here we create the source files and the directory they are in.
		// Called before each test as part of setup()
		// Could roll up into setup() if not used elsewhere.

		if (!operationsRoot.exists())
			operationsRoot.mkdirs();

		if (!autoBackupRoot.exists())
			autoBackupRoot.mkdirs();

		if (!defaultBackupRoot.exists())
			defaultBackupRoot.mkdirs();

		for (String name : regularBackupSetFileNames) {
			createDummyXmlFile(operationsRoot, name);
		}
	}

	private void createDummyXmlFile(File dir, String name) throws IOException {
		// Creates a single dummy XML file in the given directory
		File file = new File(dir, name);
		FileWriter out = new FileWriter(file);
		out.write("This is a dummy version of the " + name + " file...");

		out.close();
	}

	private Boolean existsFile(File dir, String name) {
		// Helper to check if a file in the given directory exists.
		// Could roll up into verifyBackupSetFiles if not used elsewhere.
		File file = new File(dir, name);
		return file.exists();
	}

	public void deleteTestFiles() {
		// Delete all files in the test source directory
		// Called before each test as part of setup()
		// Could roll up into setup() if not used elsewhere.
		if (operationsRoot.exists()) {
			for (File f : operationsRoot.listFiles()) {
				if (f.isFile())
					f.delete();
			}
		}
	}

	private void deleteDirectoryAndFiles(File dir) {
		// Deletes all of the files in a directory, and then the directory
		// itself.
		// This is NOT a general purpose method, as it only handles directories
		// with only files and no sub directories.
		for (File f : dir.listFiles()) {
			// Delete files first
			if (f.isFile()) {
				f.delete();
			}
		}

		dir.delete();
	}

	public void verifyBackupSetAgainst(File srcDir, String srcSet, File dstDir,
			String dstSet) {
		// Defaults to the test file names
		verifyBackupSetAgainst(srcDir, srcSet, dstDir, dstSet,
				regularBackupSetFileNames);
	}

	public void verifyBackupSetAgainst(File srcDir, String srcSet, File dstDir,
			String dstSet, String[] fileNames) {
		// Does a number of tests on the files in a backup set directory and
		// compares them to the source directory.
		// Both source and destination can have a setName as we have to test
		// both copies and restores.

		// If the src or dst Set Name is the empty string, "", new File() will
		// simply ignore it and use just the directory.
		srcDir = new File(srcDir, srcSet);
		dstDir = new File(dstDir, dstSet);

		// Verify each file in turn.
		for (String fileName : fileNames) {
			verifyDstFileEqualsScrFile(fileName, srcDir, dstDir);
		}
	}

	private void verifyBackupFileCount(File dir, String setName,
			int expectedCount) {
		// Verifies if the actual number of files in the destination directory
		// equals
		// the expected count.
		dir = new File(dir, setName);
		int actualCount = dir.list().length;

		assertEquals(expectedCount, actualCount);
	}

	private void verifyDstFileEqualsScrFile(String fileName, File srcDir,
			File dstDir) {
		// Verifies if the named file in the destination directory is the same
		// as the same named file in the source directory.
		// A variety of tests are done to determine equality.
		File src = new File(srcDir, fileName);
		File dst = new File(dstDir, fileName);

		Assert.assertTrue(fileName + " file must exist", dst.exists());

		Assert.assertTrue(fileName + " file must have length > 0",
				dst.length() > 0);

		Assert.assertEquals(fileName + " file size must be the same",
				src.length(), dst.length());

		// Enable after Backup updates modified date
		Assert.assertEquals(fileName + " file modifed date must be the same",
				src.lastModified(), dst.lastModified());

		// We could extend this to compare the file contents, but I don't think
		// this is necessary if the file sizes and dates match.
	}

	// And now the actual tests themselves.....

	// First some common tests of BackupBase
	// Need to instantiate DefaultBackup as BackupBase is abstract.

	// Make sure that we are working with the exact set of file names that we
	// expect.
	public void testGetBackupSetFileNames() {
		BackupBase backup = new DefaultBackup();
		String[] names = backup.getBackupSetFileNames();

		Assert.assertEquals("Backup set file name count", 6, names.length);

		Assert.assertEquals("Operations.xml", names[0]);
		Assert.assertEquals("OperationsCarRoster.xml", names[1]);
		Assert.assertEquals("OperationsEngineRoster.xml", names[2]);
		Assert.assertEquals("OperationsLocationRoster.xml", names[3]);
		Assert.assertEquals("OperationsRouteRoster.xml", names[4]);
		Assert.assertEquals("OperationsTrainRoster.xml", names[5]);
	}

	// public void testTestBackupSetFileNames() {
	// String[] names = testBackupSetFileNames;
	//
	// Assert.assertEquals("Test Backup set file name count", 6, names.length);
	//
	// Assert.assertEquals("NEW_TEST_Operations.xml", names[0]);
	// Assert.assertEquals("NEW_TEST_OperationsCarRoster.xml", names[1]);
	// Assert.assertEquals("NEW_TEST_OperationsEngineRoster.xml", names[2]);
	// Assert.assertEquals("NEW_TEST_OperationsLocationRoster.xml", names[3]);
	// Assert.assertEquals("NEW_TEST_OperationsRouteRoster.xml", names[4]);
	// Assert.assertEquals("NEW_TEST_OperationsTrainRoster.xml", names[5]);
	// }

	public void testTestFilesCreated() {
		// Make sure we can create our test files correctly

		// Make sure the directories exist
		Assert.assertTrue("Test directory must exist", operationsRoot.exists());
		Assert.assertTrue("Auto backup directory must exist",
				autoBackupRoot.exists());
		Assert.assertTrue("Default backup directory must exist",
				defaultBackupRoot.exists());

		// and the test files
		verifyBackupSetAgainst(operationsRoot, "", operationsRoot, "");

		// and the getSourceFileCount method as well
		BackupBase backup = new DefaultBackup();
		int count = backup.getSourceFileCount(operationsRoot);
		assertEquals("Test source file count", 6, count);

	}

	public void testBasicCopyBackupSet() throws IOException {
		// Test that we can actually copy the files of a backup set to a
		// different directory. This is the heart of the Backup class.
		BackupBase backup = new DefaultBackup();
		String setName = "NEW Test Backup Set 01";
		File setDir = new File(defaultBackupRoot, setName);

		// Enable this only after we decide how to handle extra files.
		// // Throw in an extra file in the source to test of we only copy the
		// files we want.
		// createDummyXmlFile(testRoot, "This is an EXTRA file.xml");
		// createDummyXmlFile(testRoot, "This is NOT an xml file.txt");

		backup.copyBackupSet(operationsRoot, setDir);

		// Check that they got there
		verifyBackupSetAgainst(operationsRoot, "", defaultBackupRoot, setName);

		// And that we have the right count of files
		verifyBackupFileCount(defaultBackupRoot, setName,
				backup.getBackupSetFileNames().length);
	}

	public void testBasicCopyBackupSetWithNoFiles() throws IOException {
		// Test copying with none of the files in the source, representing the
		// state after a Reset() operation.
		BackupBase backup = new DefaultBackup();
		String setName = "NEW Test Backup Set 02";
		File setDir = new File(defaultBackupRoot, setName);

		deleteTestFiles();
		int count = backup.getSourceFileCount(operationsRoot);
		assertEquals("SHould be zero files after delete()", 0, count);

		backup.copyBackupSet(operationsRoot, setDir);

		// Should just return without creating the dest dir or throwing an
		// exception.
		Boolean exists = existsFile(defaultBackupRoot, setName);
		assertFalse(exists);

	}

	// private void createDummyXmlFile(File dir, String name) throws IOException
	// {

	public void testBasicCopyBackupSetWithMissingFiles() throws IOException {
		// Test copying with only some of the files in the source. This MAY be
		// a valid state, so no exception should be thrown.
		BackupBase backup = new DefaultBackup();
		String setName = "NEW Test Backup Set 03";
		File setDir = new File(defaultBackupRoot, setName);

		// Clear out any files, and create just one
		deleteTestFiles();
		createDummyXmlFile(operationsRoot, regularBackupSetFileNames[0]);

		backup.copyBackupSet(operationsRoot, setDir);

		// Should NOT throw an exception, and the destination dir should exist.
		Boolean exists = existsFile(defaultBackupRoot, setName);
		assertTrue(exists);
	}

	public void testBasicCopyBackupSetWithExtraFiles() throws IOException {
		// Test copying with all of the Operations files, plus some extra files.
		// SHould only copy the Operations files.
		BackupBase backup = new DefaultBackup();
		String setName = "NEW Test Backup Set 04";
		File setDir = new File(defaultBackupRoot, setName);

		// Enable this only after we decide how to handle extra files.
		// Throw in an extra file in the source to test of we only copy the
		// files we want.
		createDummyXmlFile(operationsRoot, "This is an EXTRA file.xml");
		createDummyXmlFile(operationsRoot, "This is NOT an xml file.txt");

		backup.copyBackupSet(operationsRoot, setDir);

		// Check that they got there
		verifyBackupSetAgainst(operationsRoot, "", defaultBackupRoot, setName);

		// And that we have the right count of files, skipping the extra Xml
		// file
		verifyBackupFileCount(defaultBackupRoot, setName,
				backup.getBackupSetFileNames().length);
	}

	public void testBackupToSpecificDirectory() throws IOException {
		// Does a backup to a specific directory.
		// The backup set directory is given as a File object.
		// This simulates using a FIleChooser to select the destination
		// directory.
		BackupBase backup = new DefaultBackup();

		File dir = new File(operationsRoot, "Specific Backup Dir");

		backup.backupFilesToDirectory(dir);

		// Check that they got there
		verifyBackupSetAgainst(operationsRoot, "", dir, "");
	}

	// Comment out for now until I can learn what constitutes an invalid
	// filename on the CI build server....
	// Having "<<" in a file name fails under Windows, but maybe not under
	// Linux???
	// public void testBackupToInvalidDirectory() {
	// // Does a backup to a specific directory that has an invalid name.
	// // The backup set directory is given as a File object.
	// // This simulates using a FIleChooser to select the destination
	// // directory.
	// BackupBase backup = new DefaultBackup();
	//
	// File dir = new File(operationsRoot, "Invalid Name<<>>");
	//
	// try {
	// backup.backupFilesToDirectory(dir);
	//
	// fail("Expected exception to be thrown.");
	// } catch (Exception ex) {
	// // Maybe we should check what type of exception was caught???
	// }
	// }

	public void testRestoreFilesFromSpecificDirectory() throws IOException {
		// Simulates using a FileChooser to select a directory to restore from.
		BackupBase backup = new DefaultBackup();

		File dir = new File(operationsRoot, "Specific Backup Dir");

		backup.backupFilesToDirectory(dir);

		deleteTestFiles();

		backup.restoreFilesFromDirectory(dir);

		verifyBackupSetAgainst(dir, "", operationsRoot, "");
	}

	public void testResetFiles() {

		BackupBase backup = new DefaultBackup();

		// Now you see them...
		for (String name : regularBackupSetFileNames) {
			// Make sure each file does exist
			Assert.assertTrue(name + " file should exist",
					existsFile(operationsRoot, name));
		}

		backup.deleteOperationsFiles();

		// and now you don't....
		for (String name : regularBackupSetFileNames) {
			// Make sure each file does NOT exist
			Assert.assertFalse(name + " file should not exist",
					existsFile(operationsRoot, name));
		}
	}

	public void testLoadDemoFiles() throws IOException {
		BackupBase backup = new DefaultBackup();

		backup.deleteOperationsFiles();

		backup.loadDemoFiles();

		// check that we have 6 or more files...
		assertTrue("File count", operationsRoot.listFiles().length >= 6);

		verifyBackupSetAgainst(new File(XmlFile.xmlDir(), "demoOperations"),
				"", operationsRoot, "", regularBackupSetFileNames);

		// Also need to make sure we copied over the demo panel file
		verifyBackupSetAgainst(new File(XmlFile.xmlDir(), "demoOperations"),
				"", operationsRoot, "",
				new String[] { "Operations Demo Panel.xml" });
	}

	// Now tests of the DefaultBackup class.....

	public void testCreateDefaultBackupInstance() {
		// Basic test to make sure we can instantiate the DefaultBackup class
		BackupBase backup = new DefaultBackup();
		Assert.assertNotNull("Default constructor", backup);

		// Make sure default roots got setup OK
		File root = backup.getBackupRoot();
		Assert.assertNotNull("DEfault root must be set", root);
		Assert.assertTrue("Default root dir must exist", root.exists());

		String prefsDir = XmlFile.prefsDir();
		String opsDirName = OperationsXml.getOperationsDirectoryName();
		File opsRoot = new File(prefsDir, opsDirName);

		File expectedRoot = new File(opsRoot, "backups");
		Assert.assertEquals("Default root", expectedRoot, root);
	}

	public void testDefaultBackupWithName() throws IOException {
		// Does a backup to the backups directory
		// The name of the backup set is given.
		BackupBase backup = new DefaultBackup();

		String setName = backup.suggestBackupSetName();
		backup.backupFilesToSetName(setName);

		// Check that they got there
		verifyBackupSetAgainst(operationsRoot, "", defaultBackupRoot, setName);
	}

	public void testDefaultBackupWithName2() throws IOException {
		// Does a backup to the backups directory
		// The name of the backup set is given.
		BackupBase backup = new DefaultBackup();

		String setName = backup.suggestBackupSetName();
		backup.backupFilesToSetName(setName);

		// Check that they got there
		verifyBackupSetAgainst(operationsRoot, "", defaultBackupRoot, setName);
	}

	public void testDefaultBackupWithNullName() {
		// Tries a backup to the backups directory
		// The name of the backup set is null.
		BackupBase backup = new DefaultBackup();

		try {
			backup.backupFilesToSetName((String) null);
			fail("Expected exception to be thrown.");
		} catch (Exception ex) {
		}
	}

	public void testDefaultBackupWithEmptyName() {
		// Tries a backup to the backups directory
		// The name of the backup set is empty ("")
		BackupBase backup = new DefaultBackup();

		// String setName = backup.suggestBackupSetName();

		try {
			backup.backupFilesToSetName("");
			fail("Expected exception to be thrown.");
		} catch (Exception ex) {
		}
	}

	public void testSuggestedDefaultBackupName() throws IOException {
		// Tests creating the suggested backup set names that account for
		// existing backup sets.
		// This probably should test for what happens after 99, but I'll leave
		// that for later.
		String suggestedName;

		// Get the suggested name with no backups in place
		Calendar now = Calendar.getInstance();

		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DATE);

		String date2 = String.format("%d_%02d_%02d", year, month, day);

		String expected = date2 + "_00";

		BackupBase backup = new DefaultBackup();

		suggestedName = backup.suggestBackupSetName();
		Assert.assertEquals("Suggested default backup set name", expected,
				suggestedName);

		// Again with one backup
		backup.backupFilesToSetName(suggestedName);

		expected = date2 + "_01";
		suggestedName = backup.suggestBackupSetName();
		Assert.assertEquals("Suggested default backup set name", expected,
				suggestedName);

		// and again with two backups
		backup.backupFilesToSetName(suggestedName);

		expected = date2 + "_02";
		suggestedName = backup.suggestBackupSetName();
		Assert.assertEquals("Suggested default backup set name", expected,
				suggestedName);

	}

	// Test restores by doing a backup, deleting the source files, doing a
	// restore and verifying the new source files.
	public void testRestoreFilesFromDefault() throws IOException {
		BackupBase backup = new DefaultBackup();
		String setName = backup.suggestBackupSetName();
		backup.backupFilesToSetName(setName);

		deleteTestFiles();

		backup.restoreFilesFromSetName(setName);

		verifyBackupSetAgainst(defaultBackupRoot, setName, operationsRoot, "");
	}

	public void testDefaultBackupSetList() throws IOException {
		
		// confirm that all directories have been deleted
		Assert.assertTrue("Default directory exists", defaultBackupRoot.exists());
		Assert.assertEquals("Confirm directory is empty", defaultBackupRoot.list().length, 0);
		Assert.assertEquals("Confirm auto back up directory is empty", autoBackupRoot.list().length, 0);

		// Make three backups and then get the list of set names
		BackupBase backup = new DefaultBackup();

		String[] expectedList = new String[3];

		String setName;
		for (int i = 0; i < 3; i++) {
			setName = backup.suggestBackupSetName();
			backup.backupFilesToSetName(setName);
			expectedList[i] = setName;
		}
		
		Assert.assertEquals("Confirm default directory has the right number of files", defaultBackupRoot.list().length, 3);
		Assert.assertEquals("Confirm auto back up directory is empty", autoBackupRoot.list().length, 0);

		String[] actualList = backup.getBackupSetList();

		for (int i = 0; i < 3; i++) {
			Assert.assertEquals("Default set list", expectedList[i],
					actualList[i]);
		}
	}

	public void testIfDefaultBackupSetExists() throws IOException {
		// Create a default backup set then see if it exists.
		BackupBase backup = new DefaultBackup();

		String defName = backup.suggestBackupSetName();

		// Make sure it does not already exist
		Assert.assertFalse("Set should not exist yet",
				backup.checkIfBackupSetExists(defName));

		backup.backupFilesToSetName(defName);

		Assert.assertTrue("Set should exist now",
				backup.checkIfBackupSetExists(defName));
	}

	// And now the tests for the AutoBackup class...

	public void testCreateAutoBackupInstance() {
		// Basic test to make sure we can instantiate the DefaultBackup class
		BackupBase backup = new AutoBackup();
		Assert.assertNotNull("Default constructor", backup);

		// Make sure default roots got setup OK
		File root = backup.getBackupRoot();
		Assert.assertNotNull("Auto root must be set", root);
		Assert.assertTrue("Auto root dir must exist", root.exists());

		String prefsDir = XmlFile.prefsDir();
		String opsDirName = OperationsXml.getOperationsDirectoryName();
		File opsRoot = new File(prefsDir, opsDirName);

		File expectedRoot = new File(opsRoot, "autoBackups");
		Assert.assertEquals("Automatic root", expectedRoot, root);
	}

	public void testAutoBackupWithName() throws IOException {
		// Does a backup to the autoBackups directory
		// The name of the backup set is given.
		BackupBase backup = new AutoBackup();

		String setName = backup.suggestBackupSetName();
		backup.backupFilesToSetName(setName);

		// Check that they got there
		verifyBackupSetAgainst(operationsRoot, "", autoBackupRoot, setName);
	}

	public void testAutoBackupAfterResetWithNoFiles() throws IOException {
		// Should not cause a problem if there are no files to autobackup.
		AutoBackup backup = new AutoBackup();

		backup.deleteOperationsFiles();

		// Now try to back up nothing.
		backup.autoBackup();
	}

	public void testSuggestedAutoBackupName() throws IOException {
		// Tests creating the suggested backup set names that account for
		// existing backup sets.
		// This probably should test for what happens after 99, but I'll leave
		// that for later.
		String suggestedName;

		// Get the suggested name with no backups in place
		Calendar now = Calendar.getInstance();

		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DATE);

		String date2 = String.format("%d_%02d_%02d", year, month, day);

		String expected = date2 + "_00";

		BackupBase backup = new AutoBackup();

		suggestedName = backup.suggestBackupSetName();
		Assert.assertEquals("Suggested auto backup set name 00", expected,
				suggestedName);

		// Again with one backup
		backup.backupFilesToSetName(suggestedName);

		expected = date2 + "_01";
		suggestedName = backup.suggestBackupSetName();
		Assert.assertEquals("Suggested auto backup set name 01", expected,
				suggestedName);

		// and again with two backups
		backup.backupFilesToSetName(suggestedName);

		expected = date2 + "_02";
		suggestedName = backup.suggestBackupSetName();
		Assert.assertEquals("Suggested auto backup set name 02", expected,
				suggestedName);

	}

	// Test restores by doing a backup, deleting the source files, doing a
	// restore and verifying the new source files.
	public void testRestoreFilesFromAuto() throws IOException {
		BackupBase backup = new AutoBackup();
		String setName = backup.suggestBackupSetName();
		backup.backupFilesToSetName(setName);

		deleteTestFiles();

		backup.restoreFilesFromSetName(setName);

		verifyBackupSetAgainst(autoBackupRoot, setName, operationsRoot, "");
	}

	public void testAutoBackupSetList() throws IOException {
		
		// confirm that all directories have been deleted
		Assert.assertTrue("Auto backup directory exists", autoBackupRoot.exists());
		Assert.assertEquals("Confirm directory is empty", autoBackupRoot.list().length, 0);
		Assert.assertEquals("Confirm default backup directory is empty", defaultBackupRoot.list().length, 0);
		
		// Make three backups and then get the list of set names
		BackupBase backup = new AutoBackup();

		String[] expectedList = new String[3];

		String setName;
		for (int i = 0; i < 3; i++) {
			setName = backup.suggestBackupSetName();
			backup.backupFilesToSetName(setName);
			expectedList[i] = setName;
		}
		
		Assert.assertEquals("Auto backup directory has the right number of files", autoBackupRoot.list().length, 3);
		Assert.assertEquals("Confirm default backup directory is empty", defaultBackupRoot.list().length, 0);

		String[] actualList = backup.getBackupSetList();

		for (int i = 0; i < 3; i++) {
			Assert.assertEquals("Default set list", expectedList[i],
					actualList[i]);
		}
	}

	public void testIfAutoBackupSetExists() throws IOException {
		// Create a auto backup set then see if it exists.
		BackupBase backup = new AutoBackup();

		String defName = backup.suggestBackupSetName();

		// Make sure it does not already exist
		Assert.assertFalse("Set should not exist yet",
				backup.checkIfBackupSetExists(defName));

		backup.backupFilesToSetName(defName);

		Assert.assertTrue("Set should exist now",
				backup.checkIfBackupSetExists(defName));
	}

	public void testAutoBackup() throws IOException {
		// Does a backup to the autoBackups directory.
		// The name of the backup set is generated internally.
		AutoBackup backup = new AutoBackup();

		String setName = backup.suggestBackupSetName();
		backup.autoBackup();

		// Check that they got there
		verifyBackupSetAgainst(operationsRoot, "", autoBackupRoot, setName);
	}
}
