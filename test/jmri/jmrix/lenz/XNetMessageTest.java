package jmri.jmrix.lenz;

import java.io.*;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetMessageTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.XNetMessage class
 * @author			Bob Jacobsen
 * @version         $Revision: 1.1 $
 */
public class XNetMessageTest extends TestCase {

    public void testCtor() {
        XNetMessage m = new XNetMessage(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    // check opcode inclusion in message
    public void testOpCode() {
        XNetMessage m = new XNetMessage(5);
        m.setOpCode(4);
        Assert.assertEquals("read=back op code", 4, m.getOpCode());
        Assert.assertEquals("stored op code", 0x43, m.getElement(0));
    }
    // check parity operations
    public void testParity() {
        XNetMessage m;
        m = new XNetMessage(3);
        m.setElement(0,0x21);
        m.setElement(1,0x21);
        m.setParity();
        Assert.assertEquals("parity set test 1", 0, m.getElement(2));
       Assert.assertEquals("parity check test 1", true, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0,0x21);
        m.setElement(1,~0x21);
        m.setParity();
        Assert.assertEquals("parity set test 2", 0xFF, m.getElement(2));
        Assert.assertEquals("parity check test 2", true, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0,0x18);
        m.setElement(1,0x36);
        m.setParity();
        Assert.assertEquals("parity set test 3", 0x2E, m.getElement(2));
        Assert.assertEquals("parity check test 3", true, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0,0x87);
        m.setElement(1,0x31);
        m.setParity();
        Assert.assertEquals("parity set test 4", 0xB6, m.getElement(2));
        Assert.assertEquals("parity check test 4", true, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0,0x18);
        m.setElement(1,0x36);
        m.setElement(2,0x0e);
        Assert.assertEquals("parity check test 5", false, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0,0x18);
        m.setElement(1,0x36);
        m.setElement(2,0x8e);
        Assert.assertEquals("parity check test 6", false, m.checkParity());
    }

	// from here down is testing infrastructure

	public XNetMessageTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {XNetMessageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetMessageTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetMessageTest.class.getName());

}
