//OperationsBackupGuiTest.java

package jmri.jmrit.operations.setup;

import junit.framework.Test;
import junit.framework.TestSuite;
import java.util.Locale;

/**
 * Tests for the Operations Setup Backup GUI class.
 * 
 * Test have NOT been written to exercise the Backup and Restore menu items on
 * the Setup frame. These operations call the main backup and restore classes,
 * which have tests.
 * 
 * There are only simple tests to ensure that the dialogs can be created and
 * initialized.
 * 
 * The dialog based classes are very simple, so there is not much value in
 * playing around with using the GUI controls to exercise the backup classes.
 * 
 * 
 * @author Gregory Madsen Copyright (C) 2012
 */
public class OperationsBackupGuiTest extends jmri.util.SwingTestCase {

	// private File operationsRoot;
	// private File defaultBackupRoot;
	// private File autoBackupRoot;

	// OperationsBackupTest tester;

	public OperationsBackupGuiTest(String s) {
		super(s);

		// Create a Backup tester instance to do various checks on the backups
		// and restores.
		// tester = new OperationsBackupTest("For GUI tests");

		// Initialize our root directories from the Tester
		// operationsRoot = tester.getOperationsRoot();
		// defaultBackupRoot = tester.getDefaultBackupRoot();
		// autoBackupRoot = tester.getAutoBackupRoot();
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = { "-noloading",
				OperationsBackupGuiTest.class.getName() };
		junit.swingui.TestRunner.main(testCaseName);
	}

	// Ensure minimal setup for log4J
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		apps.tests.Log4JFixture.setUp();

		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);

		// // Repoint OperationsXml to JUnitTest subdirectory
		// OperationsXml.setOperationsDirectoryName("operations" +
		// File.separator
		// + "JUnitTest");
		//
		// // Create some test files to backup
		// tester.setUp();
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() throws Exception {
		apps.tests.Log4JFixture.tearDown();

		// tester.tearDown();

		super.tearDown();
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsBackupGuiTest.class);
		return suite;
	}

	public void testCreateBackupDialog() {
		BackupDialog dlg = new BackupDialog();
		dlg.setLocationRelativeTo(null);
		dlg.setModal(false);
		dlg.setVisible(true);

		dlg.dispose();
	}

	public void testCreateRestoreDialog() {
		RestoreDialog dlg = new RestoreDialog();
		dlg.setLocationRelativeTo(null);
		dlg.setModal(false);
		dlg.setVisible(true);

		dlg.dispose();
	}

	public void testCreateManageBackupsDialog() {
		ManageBackupsDialog dlg = new ManageBackupsDialog();
		dlg.setLocationRelativeTo(null);
		dlg.setModal(false);
		dlg.setVisible(true);

		dlg.dispose();
	}

	// private Frame findFrameByClassName(String className) {
	// // This probably should move to some utility class.......
	// Frame[] frames = Frame.getFrames();
	//
	// Frame frame = null;
	// for (Frame f : frames) {
	// if (f.getClass().getName().endsWith("." + className)) {
	// frame = f;
	// }
	// }
	//
	// return frame;
	// }

	// public void testBackupButtonWithDefaultName() {
	// OperationsSetupFrame f = new OperationsSetupFrame();
	// f.setLocation(0, 0); // entire panel must be visible for tests to work
	// // properly
	// f.initComponents();
	//
	// getHelper()
	// .enterClickAndLeave(new MouseEventData(this, f.backupButton));
	//
	// // Make sure the Backup frame was created.
	// BackupFrame bf = (BackupFrame) findFrameByClassName("BackupFrame");
	//
	// Assert.assertNotNull("BackupFrame not found", bf);
	//
	// BackupBase backup = new DefaultBackup();
	// String setName = backup.suggestBackupSetName();
	//
	// // Make sure the selected item in the list is the same
	// String dialogSetName = bf.backupSetNameTextField.getText();
	//
	// Assert.assertEquals("Suggested set name", setName, dialogSetName);
	//
	// // Now click the Backup button to make the backup
	// getHelper().enterClickAndLeave(
	// new MouseEventData(this, bf.backupButton));
	//
	// // This will use the Test backup file names.
	// tester.verifyBackupSetAgainst(operationsRoot, "", defaultBackupRoot,
	// setName);
	//
	// bf.dispose();
	// f.dispose();
	// }

	// public void testBackupButtonWithSuppliedName() {
	// OperationsSetupFrame f = new OperationsSetupFrame();
	// f.setLocation(0, 0); // entire panel must be visible for tests to work
	// // properly
	// f.initComponents();
	//
	// getHelper()
	// .enterClickAndLeave(new MouseEventData(this, f.backupButton));
	//
	// // Make sure the Backup frame was created.
	// BackupFrame bf = (BackupFrame) findFrameByClassName("BackupFrame");
	//
	// Assert.assertNotNull("BackupFrame not found", bf);
	//
	// String setName = "Test Backup 99";
	//
	// // Make sure the selected item in the list is the same
	// bf.backupSetNameTextField.setText(setName);
	// String dialogSetName = bf.backupSetNameTextField.getText();
	//
	// Assert.assertEquals("Provided set name", setName, dialogSetName);
	//
	// // Now click the Backup button to make the backup
	// getHelper().enterClickAndLeave(
	// new MouseEventData(this, bf.backupButton));
	//
	// // This will use the Test backup file names.
	// tester.verifyBackupSetAgainst(operationsRoot, "", defaultBackupRoot,
	// setName);
	//
	// bf.dispose();
	// f.dispose();
	// }

	// public void testRestoreButtonWithDefaultName() throws IOException {
	// // Get the name that will be used for the backup
	// BackupBase backup = new DefaultBackup();
	// String setName = backup.suggestBackupSetName();
	//
	// backup.backupFilesTo2(setName);
	//
	// OperationsSetupFrame f = new OperationsSetupFrame();
	// f.setLocation(0, 0); // entire panel must be visible for tests to work
	// // properly
	// f.initComponents();
	//
	// getHelper().enterClickAndLeave(
	// new MouseEventData(this, f.restoreButton));
	//
	// // Make sure the Restore frame was created.
	// RestoreFrame rf = (RestoreFrame) findFrameByClassName("RestoreFrame");
	//
	// Assert.assertNotNull("RestoreFrame not found", rf);
	//
	// // Make sure the selected item in the list is the same
	// BackupSet set = (BackupSet) (rf.backupSetsComboBox.getSelectedItem());
	// Assert.assertEquals("Restore set name", setName, set.getSetName());
	//
	// // Delete our working files
	// tester.deleteTestFiles();
	//
	// // Click the Restore button to do the actual restore
	// getHelper().enterClickAndLeave(
	// new MouseEventData(this, rf.restoreButton));
	//
	// // Now verify that the files were restored OK
	// tester.verifyBackupSetAgainst(defaultBackupRoot, setName, operationsRoot,
	// "");
	//
	// rf.dispose();
	// f.dispose();
	// }

	// public void testVerifyBackupSetNameListAndRestore() throws IOException {
	//
	// // Make three backups then check if the combobox list contains exactly
	// // those three names
	// BackupBase backup = new DefaultBackup();
	// String setName1 = backup.suggestBackupSetName();
	//
	// backup.backupFilesTo2(setName1);
	//
	// String setName2 = backup.suggestBackupSetName();
	// backup.backupFilesTo2(setName2);
	//
	// // Get the demo files for the third set so that we can tell which set
	// // gets restored.
	// tester.deleteTestFiles();
	// backup.loadDemoFiles2();
	//
	// String setName3 = backup.suggestBackupSetName();
	// backup.backupFilesTo2(setName3);
	//
	// // Now open the Setup frame and then the Restore frame
	// OperationsSetupFrame f = new OperationsSetupFrame();
	// f.setLocation(0, 0); // entire panel must be visible for tests to work
	// // properly
	// f.initComponents();
	//
	// getHelper().enterClickAndLeave(
	// new MouseEventData(this, f.restoreButton));
	//
	// // Make sure the Restore frame was created.
	// RestoreFrame rf = (RestoreFrame) findFrameByClassName("RestoreFrame");
	//
	// Assert.assertNotNull("RestoreFrame not found", rf);
	//
	// // Make sure the selected item in the list is the same
	// int count = rf.backupSetsComboBox.getItemCount();
	// Assert.assertEquals("Restore set name count", 3, count);
	//
	//
	// BackupSet set;
	// set = (BackupSet) rf.backupSetsComboBox.getItemAt(0);
	// Assert.assertEquals("Set 1", setName1, set.getSetName());
	//
	// set = (BackupSet) rf.backupSetsComboBox.getItemAt(1);
	// Assert.assertEquals("Set 2", setName2, set.getSetName());
	//
	// set = (BackupSet) rf.backupSetsComboBox.getItemAt(2);
	// Assert.assertEquals("Set 3", setName3, set.getSetName());
	//
	// // Restore the last set
	// tester.deleteTestFiles();
	//
	// rf.backupSetsComboBox.setSelectedIndex(2);
	//
	// getHelper().enterClickAndLeave(
	// new MouseEventData(this, rf.restoreButton));
	//
	// // Now verify that the files were restored OK
	// tester.verifyBackupSetAgainst(defaultBackupRoot, setName3,
	// operationsRoot,
	// "", tester.getRegularBackupSetFileNames());
	//
	// rf.dispose();
	// }
}
