// NceTest.java

package jmri.jmrix.nce;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

/**
 * tests for the jmri.jmrix.nce package
 * @author			Bob Jacobsen
 * @version   $Revision: 1.4 $
 */
public class NceTest extends TestCase {

    // from here down is testing infrastructure

    public NceTest(String s) {
        super(s);
    }

    // a dummy test to avoid JUnit warning
    public void testDemo() {
        assertTrue(true);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.nce.NceTest");
        suite.addTest(jmri.jmrix.nce.NceTurnoutTest.suite());
        suite.addTest(jmri.jmrix.nce.NceTurnoutManagerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceAIUTest.suite());
        suite.addTest(jmri.jmrix.nce.ncemon.NceMonFrameTest.suite());
        suite.addTest(jmri.jmrix.nce.NceProgrammerTest.suite());
        suite.addTest(jmri.jmrix.nce.packetgen.NcePacketGenFrameTest.suite());
        suite.addTest(jmri.jmrix.nce.NceTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceMessageTest.suite());
        suite.addTest(jmri.jmrix.nce.NceReplyTest.suite());
        suite.addTest(jmri.jmrix.nce.NcePowerManagerTest.suite());
        return suite;
    }

}
