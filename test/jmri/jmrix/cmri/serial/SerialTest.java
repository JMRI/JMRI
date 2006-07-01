// SerialTest.java

package jmri.jmrix.cmri.serial;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

/**
 * tests for the jmri.jmrix.cmri.serial package
 * @author			Bob Jacobsen
 * @version   $Revision: 1.1 $
 */
public class SerialTest extends TestCase {

    // from here down is testing infrastructure

    public SerialTest(String s) {
        super(s);
    }

    // a dummy test to avoid JUnit warning
    public void testDemo() {
        assertTrue(true);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.cmri.serial.SerialTest");
        suite.addTest(jmri.jmrix.cmri.serial.SerialTurnoutTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialTurnoutManagerTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialNodeTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialMessageTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialTrafficControllerTest.suite());
        return suite;
    }

}
