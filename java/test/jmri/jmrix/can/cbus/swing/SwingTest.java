// SwingTest.java
package jmri.jmrix.can.cbus.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus.swing package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class SwingTest extends TestCase {

    // from here down is testing infrastructure
    public SwingTest(String s) {
        super(s);
    }

    public void testDefinitions() {
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SwingTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.can.cbus.swing.SwingTest");
        suite.addTest(jmri.jmrix.can.cbus.swing.configtool.ConfigToolActionTest.suite());
        return suite;
    }

}
