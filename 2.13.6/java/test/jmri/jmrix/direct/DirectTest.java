// DirectTest.java

package jmri.jmrix.direct;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.direct package.
 * @author      Bob Jacobsen  Copyright 2004
 * @version   $Revision$
 */
public class DirectTest extends TestCase {

    // from here down is testing infrastructure

    public DirectTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DirectTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.direct.DirectTest");
        suite.addTest(jmri.jmrix.direct.MakePacketTest.suite());
        return suite;
    }

}
