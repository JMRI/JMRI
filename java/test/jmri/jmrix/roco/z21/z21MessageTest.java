package jmri.jmrix.roco.z21;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * z21MessageTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.z21.z21Message class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class z21MessageTest extends TestCase {

    public void testCtor() {
        z21Message m = new z21Message(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
        jmri.util.JUnitAppender.assertErrorMessage("invalid length in call to ctor");
    }

    // check opcode inclusion in message
    public void testOpCode() {
        z21Message m = new z21Message(5);
        m.setOpCode(4);
        Assert.assertEquals("read=back op code", 4, m.getOpCode());
        //opcode is stored in two bytes, lsb first.
        Assert.assertEquals("stored op code", 0x0004, m.getElement(2) + (m.getElement(3) << 8));
    }

    // Test the string constructor.
    public void testStringCtor() {
        z21Message m = new z21Message("0D 00 04 00 12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length", 12, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x0D, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x00, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0x04, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x00, m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x12, m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x34, m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0xAB, m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0x03, m.getElement(7) & 0xFF);
        Assert.assertEquals("8th byte", 0x19, m.getElement(8) & 0xFF);
        Assert.assertEquals("9th byte", 0x06, m.getElement(9) & 0xFF);
        Assert.assertEquals("10th byte", 0x0B, m.getElement(10) & 0xFF);
        Assert.assertEquals("11th byte", 0xB1, m.getElement(11) & 0xFF);
    }

    // from here down is testing infrastructure
    public z21MessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", z21MessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(z21MessageTest.class);
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
