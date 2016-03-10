package jmri.jmrix.xpa;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XpaPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaPowerManager class
 *
 * @author	Paul Bender
 * @version $Revision: 17977 $
 */
public class XpaPowerManagerTest extends TestCase {

    public void testCtor() {
        XpaPowerManager t = new XpaPowerManager();
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XpaPowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaPowerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaPowerManagerTest.class);
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
