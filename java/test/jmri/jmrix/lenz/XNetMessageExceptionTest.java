package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * XNetMessageExceptionTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetMessageException class
 *
 * @author	Paul Bender
 */
public class XNetMessageExceptionTest extends TestCase {

    public void testCtor() {

        XNetMessageException c = new XNetMessageException();
        Assert.assertNotNull(c);
    }

    public void testStringCtor() {

        XNetMessageException c = new XNetMessageException("Test Exception");
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public XNetMessageExceptionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetMessageExceptionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetMessageExceptionTest.class);
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
