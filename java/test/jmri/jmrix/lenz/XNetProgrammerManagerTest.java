package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * XNetProgrammerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetProgrammerManager class
 *
 * @author	Paul Bender
 */
public class XNetProgrammerManagerTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetProgrammerManager t = new XNetProgrammerManager(new XNetProgrammer(tc), new XNetSystemConnectionMemo(tc));
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XNetProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetProgrammerManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetProgrammerManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
