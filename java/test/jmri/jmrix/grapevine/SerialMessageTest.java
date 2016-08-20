package jmri.jmrix.grapevine;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 * @version	$Revision$
 */
public class SerialMessageTest extends TestCase {

    public void testCreate() {
        SerialMessage m = new SerialMessage();
        Assert.assertNotNull("exists", m);
    }

    public void testBytesToString() {
        SerialMessage m = new SerialMessage();
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

    public void testSetParity1() {
        SerialMessage m = new SerialMessage();
        m.setElement(0, (byte) 129);
        m.setElement(1, (byte) 90);
        m.setElement(2, (byte) 129);
        m.setElement(3, (byte) (31 & 0xF0));
        m.setParity();
        Assert.assertEquals("string compare ", "81 5A 81 1F", m.toString());
    }

    public void testSetParity2() {
        SerialMessage m = new SerialMessage();
        m.setElement(0, (byte) 226);
        m.setElement(1, (byte) 13);
        m.setElement(2, (byte) 226);
        m.setElement(3, (byte) 88);
        m.setParity();
        Assert.assertEquals("string compare ", "E2 0D E2 58", m.toString());
    }

    public void testSetParity3() {
        SerialMessage m = new SerialMessage();
        m.setElement(0, (byte) 226);
        m.setElement(1, (byte) 14);
        m.setElement(2, (byte) 226);
        m.setElement(3, (byte) 86);
        m.setParity();
        Assert.assertEquals("string compare ", "E2 0E E2 56", m.toString());
    }

    public void testSetParity4() {
        SerialMessage m = new SerialMessage();
        m.setElement(0, (byte) 226);
        m.setElement(1, (byte) 15);
        m.setElement(2, (byte) 226);
        m.setElement(3, (byte) 84);
        m.setParity();
        Assert.assertEquals("string compare ", "E2 0F E2 54", m.toString());
    }

    public void testSetParity5() {
        // observed error message
        SerialMessage m = new SerialMessage();
        m.setElement(0, (byte) 0x80);
        m.setElement(1, (byte) 98);
        m.setElement(2, (byte) 0x80);
        m.setElement(3, (byte) 0x10);
        m.setParity();
        Assert.assertEquals("string compare ", "80 62 80 10", m.toString());
    }

    public void testSetParity6() {
        // special req software version
        SerialMessage m = new SerialMessage();
        m.setElement(0, (byte) 0xE2);
        m.setElement(1, (byte) 119);
        m.setElement(2, (byte) 0xE2);
        m.setElement(3, (byte) 119);
        m.setParity();
        Assert.assertEquals("string compare ", "E2 77 E2 77", m.toString());
    }

    public void testSetParity7() {
        // from doc page
        SerialMessage m = new SerialMessage();
        m.setElement(0, (byte) 129);
        m.setElement(1, (byte) 90);
        m.setElement(2, (byte) 129);
        m.setElement(3, (byte) 31);
        m.setParity();
        Assert.assertEquals("string compare ", "81 5A 81 1F", m.toString());
    }

    // from here down is testing infrastructure
    public SerialMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialMessageTest.class);
        return suite;
    }

}
