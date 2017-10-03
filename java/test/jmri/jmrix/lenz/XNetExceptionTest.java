package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * XNetExceptionTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetException class
 *
 * @author	Paul Bender
 */
public class XNetExceptionTest extends TestCase {

    public void testCtor() {

        XNetException c = new XNetException("Test Exception");
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public XNetExceptionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetExceptionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetExceptionTest.class);
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
