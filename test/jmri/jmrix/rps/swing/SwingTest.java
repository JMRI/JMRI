// SwingTest.java

package jmri.jmrix.rps.swing;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps.swing package.
 * @author      Bob Jacobsen  Copyright 2008
 * @version   $Revision: 1.1 $
 */
public class SwingTest extends TestCase {


   // from here down is testing infrastructure

    public SwingTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SwingTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite();
        suite.addTest(jmri.jmrix.rps.swing.AffineEntryPanelTest.suite()); // do 2nd to display in front
        suite.addTest(jmri.jmrix.rps.swing.polling.PollTableActionTest.suite()); // do 2nd to display in front
        return suite;
    }

}
