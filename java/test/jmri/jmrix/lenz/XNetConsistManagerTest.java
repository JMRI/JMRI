package jmri.jmrix.lenz;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetConsistManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetConsistManager class
 *
 * @author	Paul Bender
 */
public class XNetConsistManagerTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetConsistManager c = new XNetConsistManager(new XNetSystemConnectionMemo(tc));
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public XNetConsistManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetConsistManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetConsistManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
