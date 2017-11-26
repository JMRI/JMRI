package jmri.jmrit.roster;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster package
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2012
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
        TestSuite suite = new TestSuite("jmri.jmrit.roster.PackageTest");
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(RosterEntryTest.suite());
        suite.addTest(new JUnit4TestAdapter(RosterTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.roster.configurexml.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(CopyRosterItemActionTest.class));
        suite.addTest(RosterEntryPaneTest.suite());
        suite.addTest(new JUnit4TestAdapter(FunctionLabelPaneTest.class));
        suite.addTest(new JUnit4TestAdapter(IdentifyLocoTest.class));
        suite.addTest(jmri.jmrit.roster.swing.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(LocoFileTest.class));
        suite.addTest(new JUnit4TestAdapter(RecreateRosterActionTest.class));
        suite.addTest(new JUnit4TestAdapter(RosterConfigManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(RosterConfigPaneTest.class));
        suite.addTest(new JUnit4TestAdapter(RosterIconFactoryTest.class));
        suite.addTest(new JUnit4TestAdapter(RosterMediaPaneTest.class));
        suite.addTest(new JUnit4TestAdapter(RosterRecorderTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.roster.rostergroup.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(DeleteRosterItemActionTest.class));
        suite.addTest(new JUnit4TestAdapter(ExportRosterItemActionTest.class));
        suite.addTest(new JUnit4TestAdapter(FullBackupExportActionTest.class));
        suite.addTest(new JUnit4TestAdapter(FullBackupImportActionTest.class));
        suite.addTest(new JUnit4TestAdapter(ImportRosterItemActionTest.class));
        suite.addTest(new JUnit4TestAdapter(PrintRosterActionTest.class));
        suite.addTest(new JUnit4TestAdapter(PrintRosterEntryTest.class));
        suite.addTest(new JUnit4TestAdapter(UpdateDecoderDefinitionActionTest.class));
        suite.addTest(new JUnit4TestAdapter(RosterSpeedProfileTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
