package jmri.jmrit.operations.setup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.setup package
 *
 * @author	Bob Coleman
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.operations.setup.PackageTest"); // no tests in class itself
        suite.addTest(OperationsSetupTest.suite());
        suite.addTest(OperationsBackupTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsBackupGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutoBackupTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutoSaveTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BackupDialogTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BuildReportOptionActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BuildReportOptionFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BuildReportOptionPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ControlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DefaultBackupTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditManifestHeaderTextActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditManifestHeaderTextFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditManifestHeaderTextPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditManifestTextActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditManifestTextFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditManifestTextPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditSwitchListTextActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditSwitchListTextFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditSwitchListTextPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ManageBackupsDialogTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsSetupActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsSetupFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsSetupPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsSetupXmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OptionFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OptionPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintMoreOptionActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintMoreOptionFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintMoreOptionPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintOptionActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintOptionFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintOptionPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RestoreDialogTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetupTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BackupFilesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LoadDemoActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ManageBackupsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OptionActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ResetActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RestoreFilesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BackupSetTest.class));
        return suite;
    }

}
