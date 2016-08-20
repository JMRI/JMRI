package jmri.jmrix.tmcc;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003
 * @version	$Revision$
 */
public class SerialReplyTest extends TestCase {

    public void testCreate() {
        SerialReply m = new SerialReply();
        Assert.assertNotNull("exists", m);
    }

    public void testLength3() {
        SerialReply m = new SerialReply();
        m.setOpCode(0x81);
        m.setElement(1, 0x02);
        m.setElement(2, 0xA2);
        Assert.assertEquals("length ", 3, m.getNumDataElements());
    }

    public void testLength1() {
        SerialReply m = new SerialReply();
        m.setElement(0, 0x02);
        Assert.assertEquals("length ", 1, m.getNumDataElements());
    }

    public void testToBinaryString() {
        SerialReply m = new SerialReply();
        m.setOpCode(0x81);
        m.setElement(1, 0x02);
        m.setElement(2, 0x12);
        Assert.assertEquals("string compare ", "81 02 12", m.toString());
    }

    public void testBytesToString() {
        SerialReply m = new SerialReply();
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0x12);
        Assert.assertEquals("string compare ", "81 02 12", m.toString());
    }

    public void testToASCIIString() {
        SerialReply m = new SerialReply();
        m.setOpCode(0x54);
        m.setElement(1, 0x20);
        m.setElement(2, 0x32);
        Assert.assertEquals("string compare ", "54 20 32", m.toString());
    }

    // from here down is testing infrastructure
    public SerialReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialReplyTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialReplyTest.class);
        return suite;
    }

}
