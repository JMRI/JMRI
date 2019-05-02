package jmri.jmrit.operations.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.FileUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the new Operations Setup Backup classes used for copying and
 * restoring file backup sets. These test the AutoBackup and DefaultBackup
 * classes that are derived from BackupBase.
 * <p>
 * These tests use dummy XML files for the copying to make testing the outcomes
 * easier. It is assumed that there are other tests to verify that the correct
 * format XML files get created. This only tests the copying parts of the Backup
 * and Restore operations.
 * <p>
 * Tests include: - copying to / from regular backup location - copying to /
 * from automatic backup location
 * <p>
 * Source files are in Operations / JUnitTests.
 * <p>
 * Backup dirs are in: Operations / JUnitTests / backups and
 * <p>
 * Operations / JUnitTests / autoBackups
 * <p>
 * <p>
 * Still to do: - Need file comparison method to verify exactly that the files
 * are the same.
 *
 *
 * @author Gregory Madsen Copyright (C) 2012
 *
 */
public class OperationsBackupTest {

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

    public String[] getRegularBackupSetFileNames() {
        return regularBackupSetFileNames;
    }

    private String[] testBackupSetFileNames;

    public String[] getTestBackupSetFileNames() {
        return testBackupSetFileNames;
    }

    /**
     * Test-by test initialization.
     * @throws IOException if thrown by {@link #createTestFiles()}
     */
    @Before
    public void setUp() throws IOException {
        jmri.util.JUnitUtil.setUp();

        // set the file location to temp (in the root of the build directory).
        OperationsSetupXml.setFileLocation("temp" + File.separator);

        // Set the static Operations root directory used for tests
        OperationsXml.setOperationsDirectoryName("operations" + File.separator
                + "JUnitTest");

        // Initialize our root directories as they should be so that we can
        // verify the Backup classes use the correct locations.
        //
        // Correct structure is:
        // <user>/JMRI/Operations / JUnitTest / backups
        // <user>/JMRI/Operations / JUnitTest / autoBackups
        operationsRoot = new File(OperationsXml.getFileLocation(),
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
        // set the file location to temp (in the root of the build directory).
        OperationsSetupXml.setFileLocation("temp" + File.separator);

        // Repoint OperationsSetupXml to JUnitTest subdirectory
        String tempstring = OperationsSetupXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator + "JUnitTest")) {
            OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
        }
        // Change file names to ...Test.xml
        InstanceManager.getDefault(OperationsSetupXml.class).setOperationsFileName("OperationsJUnitTest.xml");
        InstanceManager.getDefault(RouteManagerXml.class).setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        InstanceManager.getDefault(EngineManagerXml.class).setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        InstanceManager.getDefault(CarManagerXml.class).setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        InstanceManager.getDefault(LocationManagerXml.class).setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        InstanceManager.getDefault(TrainManagerXml.class).setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        FileUtil.createDirectory("temp" + File.separator + OperationsSetupXml.getOperationsDirectoryName());

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

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
        deleteTestFiles();
    }

    // Some private helper methods......
    public void createTestFiles() throws IOException {
        // Here we create the source files and the directory they are in.
        // Called before each test as part of setup()
        // Could roll up into setup() if not used elsewhere.

        if (!operationsRoot.exists()) {
            operationsRoot.mkdirs();
        }

        if (!autoBackupRoot.exists()) {
            autoBackupRoot.mkdirs();
        }

        if (!defaultBackupRoot.exists()) {
            defaultBackupRoot.mkdirs();
        }

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
                if (f.isFile()) {
                    f.delete();
                }
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
    @Test
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

    @Test
    @Ignore("Disabled in JUnit 3")
    public void testTestBackupSetFileNames() {
        String[] names = testBackupSetFileNames;

        Assert.assertEquals("Test Backup set file name count", 6, names.length);

        Assert.assertEquals("NEW_TEST_Operations.xml", names[0]);
        Assert.assertEquals("NEW_TEST_OperationsCarRoster.xml", names[1]);
        Assert.assertEquals("NEW_TEST_OperationsEngineRoster.xml", names[2]);
        Assert.assertEquals("NEW_TEST_OperationsLocationRoster.xml", names[3]);
        Assert.assertEquals("NEW_TEST_OperationsRouteRoster.xml", names[4]);
        Assert.assertEquals("NEW_TEST_OperationsTrainRoster.xml", names[5]);
    }
    
    @Test
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

    @Test
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

    @Test
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

    @Test
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
        jmri.util.JUnitAppender.assertWarnMessage("Only 1 file(s) found in directory " + operationsRoot.getAbsolutePath());

        // Should NOT throw an exception, and the destination dir should exist.
        Boolean exists = existsFile(defaultBackupRoot, setName);
        assertTrue(exists);
    }

    @Test
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

    @Test
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
    @Test
    @Ignore("Disabled in JUnit 3")
    public void testBackupToInvalidDirectory() {
        // Does a backup to a specific directory that has an invalid name.
        // The backup set directory is given as a File object.
        // This simulates using a FIleChooser to select the destination
        // directory.
        BackupBase backup = new DefaultBackup();

        File dir = new File(operationsRoot, "Invalid Name<<>>");

        try {
            backup.backupFilesToDirectory(dir);

            fail("Expected exception to be thrown.");
        } catch (Exception ex) {
            // Maybe we should check what type of exception was caught???
        }
    }
    
    @Test
    public void testRestoreFilesFromSpecificDirectory() throws IOException {
        // Simulates using a FileChooser to select a directory to restore from.
        BackupBase backup = new DefaultBackup();

        File dir = new File(operationsRoot, "Specific Backup Dir");

        backup.backupFilesToDirectory(dir);

        deleteTestFiles();

        backup.restoreFilesFromDirectory(dir);

        verifyBackupSetAgainst(dir, "", operationsRoot, "");
    }

    @Test
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

    @Test
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
                new String[]{"Operations Demo Panel.xml"});
    }

    // Now tests of the DefaultBackup class.....
    @Test
    public void testCreateDefaultBackupInstance() {
        // Basic test to make sure we can instantiate the DefaultBackup class
        BackupBase backup = new DefaultBackup();
        Assert.assertNotNull("Default constructor", backup);

        // Make sure default roots got setup OK
        File root = backup.getBackupRoot();
        Assert.assertNotNull("DEfault root must be set", root);
        Assert.assertTrue("Default root dir must exist", root.exists());

        //String usersDir = FileUtil.getUserFilesPath();
        // we're resetting the root directory used to temp
        String usersDir = "temp" + File.separator;
        String opsDirName = OperationsXml.getOperationsDirectoryName();
        File opsRoot = new File(usersDir, opsDirName);

        File expectedRoot = new File(opsRoot, "backups");
        Assert.assertEquals("Default root", expectedRoot, root);
    }

    @Test
    public void testDefaultBackupWithName() throws IOException {
        // Does a backup to the backups directory
        // The name of the backup set is given.
        BackupBase backup = new DefaultBackup();

        String setName = backup.suggestBackupSetName();
        backup.backupFilesToSetName(setName);

        // Check that they got there
        verifyBackupSetAgainst(operationsRoot, "", defaultBackupRoot, setName);
    }

    @Test
    public void testDefaultBackupWithName2() throws IOException {
        // Does a backup to the backups directory
        // The name of the backup set is given.
        BackupBase backup = new DefaultBackup();

        String setName = backup.suggestBackupSetName();
        backup.backupFilesToSetName(setName);

        // Check that they got there
        verifyBackupSetAgainst(operationsRoot, "", defaultBackupRoot, setName);
    }

    @Test
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

    @Test
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

    @Test
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
    @Test
    public void testRestoreFilesFromDefault() throws IOException {
        BackupBase backup = new DefaultBackup();
        String setName = backup.suggestBackupSetName();
        backup.backupFilesToSetName(setName);

        deleteTestFiles();

        backup.restoreFilesFromSetName(setName);

        verifyBackupSetAgainst(defaultBackupRoot, setName, operationsRoot, "");
    }

    @Test
    public void testDefaultBackupSetList() throws IOException {

        // confirm that all directories have been deleted
        Assert.assertTrue("Default directory exists", defaultBackupRoot.exists());
        Assert.assertEquals("Confirm directory is empty", defaultBackupRoot.list().length, 0);
        Assert.assertEquals("Confirm auto back up directory is empty", autoBackupRoot.list().length, 0);

        // Make three backups and then get the list of set names
        BackupBase backup = new DefaultBackup();
        Assert.assertEquals("Confirm directory is empty", backup.getBackupSetList().length, 0);

        String[] expectedList = new String[3];

        String setName;
        for (int i = 0; i < 3; i++) {
            setName = backup.suggestBackupSetName();
            expectedList[i] = setName;
            backup.backupFilesToSetName(setName);
        }

        Assert.assertEquals("Confirm default directory has the right number of files", defaultBackupRoot.list().length, 3);
        Assert.assertEquals("Confirm default directory has the right number of files", backup.getBackupSetList().length, 3);
        Assert.assertEquals("Confirm auto back up directory is empty", autoBackupRoot.list().length, 0);

        String[] actualList = backup.getBackupSetList();

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals("Default set list", expectedList[i],
                    actualList[i]);
        }
    }

    @Test
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
    @Test
    public void testCreateAutoBackupInstance() {
        // Basic test to make sure we can instantiate the DefaultBackup class
        BackupBase backup = new AutoBackup();
        Assert.assertNotNull("Default constructor", backup);

        // Make sure default roots got setup OK
        File root = backup.getBackupRoot();
        Assert.assertNotNull("Auto root must be set", root);
        Assert.assertTrue("Auto root dir must exist", root.exists());

        // String usersDir = FileUtil.getUserFilesPath();
        // we're resetting the root directory used to temp
        String usersDir = "temp" + File.separator;
        String opsDirName = OperationsXml.getOperationsDirectoryName();
        File opsRoot = new File(usersDir, opsDirName);

        File expectedRoot = new File(opsRoot, "autoBackups");
        Assert.assertEquals("Automatic root", expectedRoot, root);
    }

    @Test
    public void testAutoBackupWithName() throws IOException {
        // Does a backup to the autoBackups directory
        // The name of the backup set is given.
        BackupBase backup = new AutoBackup();

        String setName = backup.suggestBackupSetName();
        backup.backupFilesToSetName(setName);

        // Check that they got there
        verifyBackupSetAgainst(operationsRoot, "", autoBackupRoot, setName);
    }

    @Test
    public void testAutoBackupAfterResetWithNoFiles() throws IOException {
        // Should not cause a problem if there are no files to autobackup.
        AutoBackup backup = new AutoBackup();

        backup.deleteOperationsFiles();

        // Now try to back up nothing.
        backup.autoBackup();
    }

    @Test
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
    @Test
    public void testRestoreFilesFromAuto() throws IOException {
        BackupBase backup = new AutoBackup();
        String setName = backup.suggestBackupSetName();
        backup.backupFilesToSetName(setName);

        deleteTestFiles();

        backup.restoreFilesFromSetName(setName);

        verifyBackupSetAgainst(autoBackupRoot, setName, operationsRoot, "");
    }

    @Test
    public void testAutoBackupSetList() throws IOException {

        // confirm that all directories have been deleted
        Assert.assertTrue("Auto backup directory exists", autoBackupRoot.exists());
        Assert.assertEquals("Confirm directory is empty", autoBackupRoot.list().length, 0);
        Assert.assertEquals("Confirm default backup directory is empty", defaultBackupRoot.list().length, 0);

        // Make three backups and then get the list of set names
        BackupBase backup = new AutoBackup();
        Assert.assertEquals("Confirm directory is empty", backup.getBackupSetList().length, 0);

        String[] expectedList = new String[3];

        String setName;
        for (int i = 0; i < 3; i++) {
            setName = backup.suggestBackupSetName();
            expectedList[i] = setName;
            backup.backupFilesToSetName(setName);
        }

        Assert.assertEquals("Auto backup directory has the right number of files", autoBackupRoot.list().length, 3);
        Assert.assertEquals("Auto backup directory has the right number of files", backup.getBackupSetList().length, 3);
        Assert.assertEquals("Confirm default backup directory is empty", defaultBackupRoot.list().length, 0);

        String[] actualList = backup.getBackupSetList();

        Assert.assertEquals("Confirm actual list length", actualList.length, 3);

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals("Default set list", expectedList[i],
                    actualList[i]);
        }
    }

    @Test
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

    @Test
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
