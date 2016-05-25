package jmri.jmrix.grapevine;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2008
 * @version	$Revision$
 */
public class SerialReplyTest extends TestCase {

    public void testCreate() {
        SerialMessage m = new SerialMessage();
        Assert.assertNotNull("exists", m);
    }

    public void testBytesToString() {
        SerialReply m = new SerialReply();
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

    public void testFormat1() {
        SerialReply m = new SerialReply();
        m.setElement(0, (byte) 0x00);
        m.setElement(1, (byte) 0x62);
        m.setElement(2, (byte) 0x00);
        m.setElement(3, (byte) 0x10);
        Assert.assertEquals("string compare ", "Error report from node 98: Parity Error", m.format());
    }

    public void testFormat2() {
        SerialReply m = new SerialReply();
        m.setElement(0, (byte) 0xE2);
        m.setElement(1, (byte) 0x06);
        m.setNumDataElements(2);
        Assert.assertEquals("string compare ", "Node 98 reports software version 6", m.format());
    }

    public void testParallel() {
        SerialReply m = new SerialReply();
        m.setElement(0, 128 + 98);
        m.setElement(1, 0x0E);
        m.setElement(2, 128 + 98);
        m.setElement(3, 0x56);
        Assert.assertEquals("parallel ", true, m.isFromParallelSensor());
        Assert.assertEquals("old serial ", false, m.isFromOldSerialSensor());
        Assert.assertEquals("new serial ", false, m.isFromNewSerialSensor());
    }

    public void testOldSerial() {
        SerialReply m = new SerialReply();
        m.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        m.setElement(1, 0x6F);
        m.setElement(2, 0x81);
        m.setElement(3, 0x50);
        Assert.assertEquals("parallel ", false, m.isFromParallelSensor());
        Assert.assertEquals("old serial ", true, m.isFromOldSerialSensor());
        Assert.assertEquals("new serial ", false, m.isFromNewSerialSensor());
    }

    // from here down is testing infrastructure
    public SerialReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialReplyTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialReplyTest.class);
        return suite;
    }

}
