package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XNetMessageExceptionTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetMessageException class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XNetMessageExceptionTest extends TestCase {

    public void testCtor() {

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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetMessageExceptionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(XNetMessageExceptionTest.class.getName());

}
