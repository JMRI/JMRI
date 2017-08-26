package jmri.jmrix.xpa;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * XpaTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaTrafficController class
 *
 * @author	Paul Bender
 */
public class XpaTrafficControllerTest extends TestCase {

    public void testCtor() {
        XpaTrafficController t = new XpaTrafficController();
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XpaTrafficControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaTrafficControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaTrafficControllerTest.class);
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
