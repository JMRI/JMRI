package jmri.jmrit.roster.swing;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster.swing package
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
        TestSuite suite = new TestSuite("jmri.jmrit.roster.swing.PackageTest");

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(RosterTableModelTest.suite());

        suite.addTest(jmri.jmrit.roster.swing.attributetable.PackageTest.suite());
        suite.addTest(jmri.jmrit.roster.swing.rostergroup.PackageTest.suite());
        suite.addTest(jmri.jmrit.roster.swing.speedprofile.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.roster.swing.rostertree.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GlobalRosterEntryComboBoxTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterEntryComboBoxTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterEntryListCellRendererTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterEntrySelectorPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterFrameActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterFrameStartupActionFactoryTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterGroupComboBoxTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterGroupsPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterTableTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CopyRosterGroupActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CreateRosterGroupActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DeleteRosterGroupActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RenameRosterGroupActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RemoveRosterEntryToGroupActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterEntryToGroupActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RosterMenuTest.class));
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
