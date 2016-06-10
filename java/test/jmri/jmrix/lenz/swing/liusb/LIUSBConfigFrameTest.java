package jmri.jmrix.lenz.swing.liusb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LIUSBConfigFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.liusb.LIUSBConfigFrame class
 *
 * @author	Paul Bender
 */
public class LIUSBConfigFrameTest extends TestCase {

    public void testCtor() {
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);

        LIUSBConfigFrame f = new LIUSBConfigFrame(memo);
        Assert.assertNotNull(f);
    }

    // from here down is testing infrastructure
    public LIUSBConfigFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LIUSBConfigFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LIUSBConfigFrameTest.class);
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
