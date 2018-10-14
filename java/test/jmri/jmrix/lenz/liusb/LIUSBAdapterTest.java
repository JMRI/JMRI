package jmri.jmrix.lenz.liusb;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * LIUSBAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.liusb.LIUSBAdapter class
 *
 * @author	Paul Bender
 */
public class LIUSBAdapterTest extends TestCase {

    public void testCtor() {
        LIUSBAdapter a = new LIUSBAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public LIUSBAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LIUSBAdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LIUSBAdapterTest.class);
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
