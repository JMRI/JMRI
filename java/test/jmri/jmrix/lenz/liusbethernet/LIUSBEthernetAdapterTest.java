package jmri.jmrix.lenz.liusbethernet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LIUSBEthernetAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.liusbethernet.LIUSBEthernetAdapter
 * class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class LIUSBEthernetAdapterTest extends TestCase {

    public void testCtor() {
        LIUSBEthernetAdapter a = new LIUSBEthernetAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public LIUSBEthernetAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LIUSBEthernetAdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LIUSBEthernetAdapterTest.class);
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
