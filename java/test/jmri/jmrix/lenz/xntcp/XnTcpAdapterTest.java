package jmri.jmrix.lenz.xntcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XnTcpAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.xntcp.XnTcpAdapter class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XnTcpAdapterTest extends TestCase {

    public void testCtor() {
        XnTcpAdapter a = new XnTcpAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public XnTcpAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XnTcpAdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XnTcpAdapterTest.class);
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
