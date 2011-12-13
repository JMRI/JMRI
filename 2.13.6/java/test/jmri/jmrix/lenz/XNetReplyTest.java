package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetReplyTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.XNetReply class
 * @author			Bob Jacobsen
 * @version         $Revision$
 */
public class XNetReplyTest extends TestCase {

    public void testCtor() {
        XNetReply m = new XNetReply();
        Assert.assertNotNull(m);
    }

    // Test the string constructor.
    public void testStringCtor(){
        XNetReply m = new XNetReply("12 34 AB 03 19 06 0B B1");
        Assert.assertEquals("length",8,m.getNumDataElements());
        Assert.assertEquals("0th byte",0x12,m.getElement(0)&0xFF);
        Assert.assertEquals("1st byte",0x34,m.getElement(1)&0xFF);
        Assert.assertEquals("2nd byte",0xAB,m.getElement(2)&0xFF);
        Assert.assertEquals("3rd byte",0x03,m.getElement(3)&0xFF);
        Assert.assertEquals("4th byte",0x19,m.getElement(4)&0xFF);
        Assert.assertEquals("5th byte",0x06,m.getElement(5)&0xFF);
        Assert.assertEquals("6th byte",0x0B,m.getElement(6)&0xFF);
        Assert.assertEquals("7th byte",0xB1,m.getElement(7)&0xFF);
    }

    // check parity operations
    public void testParity() {
        XNetReply m;
        m = new XNetReply("21 21 00");
        Assert.assertEquals("parity set test 1", 0, m.getElement(2));
        Assert.assertEquals("parity check test 1", true, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0,0x21);
        m.setElement(1,~0x21);
        m.setParity();
        Assert.assertEquals("parity set test 2", 0xFF, m.getElement(2));
        Assert.assertEquals("parity check test 2", true, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0,0x18);
        m.setElement(1,0x36);
        m.setParity();
        Assert.assertEquals("parity set test 3", 0x2E, m.getElement(2));
        Assert.assertEquals("parity check test 3", true, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0,0x87);
        m.setElement(1,0x31);
        m.setParity();
        Assert.assertEquals("parity set test 4", 0xB6, m.getElement(2));
        Assert.assertEquals("parity check test 4", true, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0,0x18);
        m.setElement(1,0x36);
        m.setElement(2,0x0e);
        Assert.assertEquals("parity check test 5", false, m.checkParity());

        m = new XNetReply("21 21 00");
        m.setElement(0,0x18);
        m.setElement(1,0x36);
        m.setElement(2,0x8e);
        Assert.assertEquals("parity check test 6", false, m.checkParity());
    }




	// from here down is testing infrastructure

	public XNetReplyTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetReplyTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetReplyTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetReplyTest.class.getName());

}
