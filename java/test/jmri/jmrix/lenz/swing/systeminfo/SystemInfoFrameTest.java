package jmri.jmrix.lenz.swing.systeminfo;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SystemInfoFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.systeminfo.SystemInfoFrame
 * class
 *
 * @author	Paul Bender
 */
public class SystemInfoFrameTest extends TestCase {

    public void testCtor() {
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);

        SystemInfoFrame f = new SystemInfoFrame(memo);
        Assert.assertNotNull(f);
    }

    // from here down is testing infrastructure
    public SystemInfoFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SystemInfoFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SystemInfoFrameTest.class);
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
