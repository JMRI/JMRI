// SerialTest.java

package jmri.jmrix.tmcc;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.tmcc package.
 * @author      Bob Jacobsen  Copyright 2003
 * @version   $Revision: 1.1 $
 */
public class SerialTest extends TestCase {

    // from here down is testing infrastructure

    public SerialTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.tmcc.SerialTest");
        suite.addTest(SerialTurnoutTest.suite());
        suite.addTest(SerialTurnoutManagerTest.suite());
        suite.addTest(SerialMessageTest.suite());
        suite.addTest(SerialReplyTest.suite());
        suite.addTest(SerialTrafficControllerTest.suite());
        suite.addTest(SerialAddressTest.suite());
        suite.addTest(jmri.jmrix.tmcc.serialmon.SerialMonFrameTest.suite());
        return suite;
    }

}
