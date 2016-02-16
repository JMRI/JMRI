package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetOpsModeProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetOpsModeProgrammer class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XNetOpsModeProgrammerTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetOpsModeProgrammer t = new XNetOpsModeProgrammer(5, tc);
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XNetOpsModeProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetOpsModeProgrammerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetOpsModeProgrammerTest.class);
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
