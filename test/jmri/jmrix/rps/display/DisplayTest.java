// DisplayTest.java

package jmri.jmrix.rps.display;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps.display package.
 * @author      Bob Jacobsen  Copyright 2006
 * @version   $Revision: 1.1 $
 */
public class DisplayTest extends TestCase {


    // Show the window
    public void testDisplay() {
        new DisplayAction().actionPerformed(null);
    }

    // from here down is testing infrastructure

    public DisplayTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DisplayTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(DisplayTest.class);
        return suite;
    }

}
