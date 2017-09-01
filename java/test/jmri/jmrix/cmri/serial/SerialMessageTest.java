package jmri.jmrix.cmri.serial;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class SerialMessageTest extends TestCase {

    public void testCreate() {
        SerialMessage m = new SerialMessage(1);
        Assert.assertNotNull("exists", m);
    }

    public void testToBinaryString() {
        SerialMessage m = new SerialMessage(4);
        m.setOpCode(0x81);
        m.setElement(1, 0x02);
        m.setElement(2, 0xA2);
        m.setElement(3, 0x00);
        m.setBinary(true);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

    public void testBytesToString() {
        SerialMessage m = new SerialMessage(4);
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x00);
        m.setBinary(true);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

    public void testToASCIIString() {
        SerialMessage m = new SerialMessage(5);
        m.setOpCode(0x54);
        m.setElement(1, 0x20);
        m.setElement(2, 0x32);
        m.setElement(3, 0x84);
        m.setElement(4, 0x05);
        m.setBinary(false);
        Assert.assertEquals("string compare ", "54 20 32 84 05", m.toString());
    }

    // from here down is testing infrastructure
    public SerialMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialMessageTest.class);
        return suite;
    }

}
