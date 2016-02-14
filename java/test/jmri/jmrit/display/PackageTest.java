/**
 * PackageTest.java
 *
 * Description:	tests for the jmrit.display package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010, 2015
 */
package jmri.jmrit.display;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.display");   // no tests in this class itself

        suite.addTest(jmri.jmrit.display.SchemaTest.suite());

        suite.addTest(jmri.jmrit.display.PositionableLabelTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrit.display.LinkingLabelTest.suite());
            suite.addTest(jmri.jmrit.display.MemoryIconTest.suite());
            suite.addTest(jmri.jmrit.display.MemorySpinnerIconTest.suite());
            suite.addTest(jmri.jmrit.display.PanelEditorTest.suite());
            suite.addTest(jmri.jmrit.display.ReporterIconTest.suite());
            suite.addTest(jmri.jmrit.display.RpsPositionIconTest.suite());
            suite.addTest(jmri.jmrit.display.SensorIconWindowTest.suite());
            suite.addTest(jmri.jmrit.display.SignalMastIconTest.suite());
            suite.addTest(jmri.jmrit.display.SignalSystemTest.suite());
            suite.addTest(jmri.jmrit.display.TurnoutIconWindowTest.suite());
            suite.addTest(jmri.jmrit.display.TurnoutIconTest.suite());
            suite.addTest(jmri.jmrit.display.IconEditorWindowTest.suite());
        }

        suite.addTest(jmri.jmrit.display.configurexml.PackageTest.suite());
        suite.addTest(jmri.jmrit.display.layoutEditor.PackageTest.suite());

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
