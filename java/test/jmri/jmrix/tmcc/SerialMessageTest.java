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
public class SerialMessageTest extends TestCase {

    public void testCreate() {
        SerialMessage m = new SerialMessage(1);
        Assert.assertNotNull("exists", m);
    }

    public void testToBinaryString() {
        SerialMessage m = new SerialMessage();
        m.setOpCode(0x81);
        m.setElement(1, 0x02);
        m.setElement(2, 0xA2);
        Assert.assertEquals("string compare ", "81 02 A2", m.toString());
    }

    public void testBytesToString() {
        SerialMessage m = new SerialMessage();
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        Assert.assertEquals("string compare ", "81 02 A2", m.toString());
    }

    public void testToASCIIString() {
        SerialMessage m = new SerialMessage();
        m.setOpCode(0x54);
        m.setElement(1, 0x20);
        m.setElement(2, 0x32);
        Assert.assertEquals("string compare ", "54 20 32", m.toString());
    }

    // from here down is testing infrastructure
    public SerialMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialMessageTest.class);
        return suite;
    }

}
