// RpsMonTest.java

package jmri.jmrix.rps.rpsmon;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps.rpsmon package.
 * @author      Bob Jacobsen  Copyright 2006
 * @version   $Revision: 1.2 $
 */
public class RpsMonTest extends TestCase {


    // show the window
    public void testDisplay() {
        new RpsMonAction().actionPerformed(null);
    }


    // from here down is testing infrastructure

    public RpsMonTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RpsMonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(RpsMonTest.class);
        return suite;
    }

}
