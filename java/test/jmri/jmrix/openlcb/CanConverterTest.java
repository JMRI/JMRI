package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.openlcb.can.OpenLcbCanFrame;

/**
 * Tests for making CAN frames into OpenLCB messages.
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class CanConverterTest extends TestCase {

    public void testCtors() {
        // mostly tests libraries, etc.
        new CanMessage(0x195B4000);
        new CanMessage(2, 0x195B4000);
        new CanMessage(new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0x182df000);
        new CanReply();
        new CanReply(2);
        new CanReply(new int[]{1, 2, 3, 4, 5, 6, 7, 8});
        new OpenLcbCanFrame(100);
    }

    // from here down is testing infrastructure
    public CanConverterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", CanConverterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CanConverterTest.class);
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
