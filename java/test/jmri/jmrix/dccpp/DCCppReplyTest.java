package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * DCCppReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppReply class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood (C) 2015
 */
public class DCCppReplyTest extends TestCase {

    public void testCtor() {
        DCCppReply m = new DCCppReply();
        Assert.assertNotNull(m);
    }

    // Test the string constructor.
    public void testStringCtor() {
        DCCppReply m = DCCppReply.parseDCCppReply("H 23 1");
        Assert.assertEquals("length", 6, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'H', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', m.getElement(5) & 0xFF);
    }

    // check is direct mode response
    public void testIsDirectModeResponse() {
        // CV 1 in direct mode.
        DCCppReply r = DCCppReply.parseDCCppReply("r 1234|87|23 12");
        Assert.assertTrue(r.isProgramReply());
        r = DCCppReply.parseDCCppReply("r 1234|66|23 4 1");
        Assert.assertTrue(r.isProgramReply());
        r = DCCppReply.parseDCCppReply("r 1234|82|23 4");
        Assert.assertTrue(r.isProgramReply());
    }

    // check get service mode CV Number response code.
    public void testGetServiceModeCVNumber() {
    }

    // check get service mode CV Value response code.
    public void testGetServiceModeCVValue() {
    }

    // from here down is testing infrastructure
    public DCCppReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppReplyTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppReplyTest.class);
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
