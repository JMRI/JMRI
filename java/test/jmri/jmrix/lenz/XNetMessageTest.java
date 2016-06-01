package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetMessage class
 *
 * @author	Bob Jacobsen
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

    // Test the string constructor.
    public void testStringCtor() {
        XNetMessage m = new XNetMessage("12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x12, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x34, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0xAB, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x03, m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x19, m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x06, m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0x0B, m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0xB1, m.getElement(7) & 0xFF);
    }

    public void testStringCtorEmptyString() {
        XNetMessage m = new XNetMessage("");
        Assert.assertEquals("length", 0, m.getNumDataElements());
        Assert.assertTrue("empty reply",m.toString().equals(""));
    }

    public void testCtorXNetReply(){
        XNetReply x = new XNetReply("12 34 AB 03 19 06 0B B1");
        XNetMessage m = new XNetMessage(x);
        Assert.assertEquals("length", x.getNumDataElements(), m.getNumDataElements());
        Assert.assertEquals("0th byte", x.getElement(0)& 0xFF, m.getElement(0)& 0xFF);
        Assert.assertEquals("1st byte", x.getElement(1)& 0xFF, m.getElement(1)& 0xFF);
        Assert.assertEquals("2nd byte", x.getElement(2)& 0xFF, m.getElement(2)& 0xFF);
        Assert.assertEquals("3rd byte", x.getElement(3)& 0xFF, m.getElement(3)& 0xFF);
        Assert.assertEquals("4th byte", x.getElement(4)& 0xFF, m.getElement(4)& 0xFF);
        Assert.assertEquals("5th byte", x.getElement(5)& 0xFF, m.getElement(5)& 0xFF);
        Assert.assertEquals("6th byte", x.getElement(6)& 0xFF, m.getElement(6)& 0xFF);
        Assert.assertEquals("7th byte", x.getElement(7)& 0xFF, m.getElement(7)& 0xFF);
    }

    // test setting/getting the opcode
    public void testSetAndGetOpCode(){
        XNetMessage m = new XNetMessage("12 34 56");
        Assert.assertEquals(0x1,m.getOpCode());
        m.setOpCode(0xA);
        Assert.assertEquals(0xA,m.getOpCode()); 
    }

    // test setting/getting the opcode
    public void testGetOpCodeHex(){
        XNetMessage m = new XNetMessage("12 34 56");
        Assert.assertEquals("0x1",m.getOpCodeHex());
        m.setOpCode(0xA);
        Assert.assertEquals("0xa",m.getOpCodeHex()); 
    }

    // check parity operations
    public void testParity() {
        XNetMessage m;
        m = new XNetMessage(3);
        m.setElement(0, 0x21);
        m.setElement(1, 0x21);
        m.setParity();
        Assert.assertEquals("parity set test 1", 0, m.getElement(2));
        Assert.assertEquals("parity check test 1", true, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0, 0x21);
        m.setElement(1, ~0x21);
        m.setParity();
        Assert.assertEquals("parity set test 2", 0xFF, m.getElement(2));
        Assert.assertEquals("parity check test 2", true, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0, 0x18);
        m.setElement(1, 0x36);
        m.setParity();
        Assert.assertEquals("parity set test 3", 0x2E, m.getElement(2));
        Assert.assertEquals("parity check test 3", true, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0, 0x87);
        m.setElement(1, 0x31);
        m.setParity();
        Assert.assertEquals("parity set test 4", 0xB6, m.getElement(2));
        Assert.assertEquals("parity check test 4", true, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0, 0x18);
        m.setElement(1, 0x36);
        m.setElement(2, 0x0e);
        Assert.assertEquals("parity check test 5", false, m.checkParity());

        m = new XNetMessage(3);
        m.setElement(0, 0x18);
        m.setElement(1, 0x36);
        m.setElement(2, 0x8e);
        Assert.assertEquals("parity check test 6", false, m.checkParity());
    }

    public void testGetElementBCD(){
        XNetMessage m = new XNetMessage("12 34 56");
        Assert.assertEquals("BCD value",Integer.valueOf(12),m.getElementBCD(0));
        Assert.assertEquals("BCD value",Integer.valueOf(34),m.getElementBCD(1));
        Assert.assertEquals("BCD value",Integer.valueOf(56),m.getElementBCD(2));
    }

    public void testLength(){
        XNetMessage m = new XNetMessage("12 34 56");
        Assert.assertEquals("length",3,m.length());
        m = new XNetMessage("12 34 56 78");
        Assert.assertEquals("length",4,m.length());
    }


    // test canned messages.
    public void testGetLiSpeedRequestMsg() {
       XNetMessage m = XNetMessage.getLIAddressRequestMsg(1);
       Assert.assertEquals(0xF2,m.getElement(0));
       Assert.assertEquals(0x01,m.getElement(1));
       Assert.assertEquals(1,m.getElement(2));
       Assert.assertEquals(0xF2,m.getElement(3));
    }

    // from here down is testing infrastructure
    public XNetMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetMessageTest.class);
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
