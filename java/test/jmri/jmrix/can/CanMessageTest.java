package jmri.jmrix.can;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.can.CanMessage class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class CanMessageTest extends CanMRCommonTestBase {

    @Test
    public void testCopyCtor() {
        CanMessage m1 = new CanMessage(0x12);
        m1.setExtended(true);

        CanMessage m2 = new CanMessage(m1);
        Assert.assertTrue("extended", m2.isExtended());
        Assert.assertTrue("header", m2.getHeader() == 0x12);
    }

    @Test
    public void testEqualsOp() {
        CanMessage m1 = new CanMessage(0x12);
        m1.setExtended(true);

        CanMessage m2 = new CanMessage(0x12);
        m2.setExtended(true);

        CanMessage m3 = new CanMessage(0x12);
        m3.setExtended(false);

        Assert.assertTrue("equals self", m1.equals(m1));
        Assert.assertTrue("equals copy", m1.equals(new CanMessage(m1)));
        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertTrue("equals same other way", m2.equals(m1));
        Assert.assertFalse("not equal null", m1.equals(null));
        Assert.assertFalse("not equals diff Ext", m1.equals(m3));
        
        Assert.assertTrue("equal hashcode", m1.hashCode() == m2.hashCode());
        
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "EC_UNRELATED_TYPES",
        justification = "CanReply and CanMessage are CanFrame with custom equals")
    @Test
    @SuppressWarnings("unlikely-arg-type") // Both CanReply and CanMessage are CanFrame with custom equals
    public void testEqualsReply() {
        CanMessage m1 = new CanMessage(0, 0x12);
        m1.setExtended(true);
        m1.setNumDataElements(0);

        CanReply m2 = new CanReply();
        m2.setExtended(true);
        m2.setHeader(0x12);
        m2.setNumDataElements(0);

        CanReply m3 = new CanReply();
        m3.setExtended(false);
        m3.setHeader(0x12);
        m3.setNumDataElements(0);

        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertFalse("not equals diff Ext", m1.equals(m3));
        Assert.assertTrue("equal hashcode", m1.hashCode() == m2.hashCode());
    }

    @Test
    @SuppressWarnings("unlikely-arg-type") // Both CanReply and CanMessage are CanFrame with custom equals
    public void testEqualsData() {
        CanMessage m1 = new CanMessage(0x12);
        m1.setNumDataElements(2);
        m1.setElement(0, 0x81);
        m1.setElement(1, 0x12);

        CanMessage m2 = new CanMessage(0x12);
        m2.setNumDataElements(2);
        m2.setElement(0, 0x81);
        m2.setElement(1, 0x12);

        CanMessage m3 = new CanMessage(0x12);
        m3.setNumDataElements(2);
        m3.setElement(0, 0x01);
        m3.setElement(1, 0x82);
        
        CanMessage m4 = new CanMessage(0x12);
        m4.setNumDataElements(1);
        m4.setElement(0, 0x07);        

        Assert.assertTrue("equals self", m1.equals(m1));
        Assert.assertTrue("equals copy", m1.equals(new CanMessage(m1)));
        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertFalse("not equals diff Ext", m1.equals(m3));
        Assert.assertFalse("not equals null", m1.equals(null));
        Assert.assertFalse("not equals string value", m1.equals("[12] 81 12"));
        Assert.assertFalse("not equals diff ele length", m1.equals(m4));
        Assert.assertTrue("equal hashcode", m1.hashCode() == m2.hashCode());
        
        m2.setRtr(true);
        Assert.assertFalse("not equals diff Rtr", m1.equals(m2));
        
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "EC_UNRELATED_TYPES",
        justification = "CanReply and CanMessage are CanFrame with custom equals")
    @Test
    @SuppressWarnings("unlikely-arg-type") // Both CanReply and CanMessage are CanFrame with custom equals
    public void testMessageFromReply() {
        CanReply r = new CanReply(0x55);
        r.setNumDataElements(2);
        r.setHeader(0x55);
        r.setElement(0, 0x01);
        r.setElement(1, 0x82);
        
        CanMessage m1 = new CanMessage(r);
        Assert.assertTrue("Header 0x55", m1.getHeader() == 0x55);
        Assert.assertTrue("2 Elements", m1.getNumDataElements() == 2);
        Assert.assertTrue("equals same", m1.equals(r));
        Assert.assertTrue("equal hashcode", m1.hashCode() == r.hashCode());
    }

    @Test
    public void testHeaderAccessors() {
        CanMessage m1 = new CanMessage(0x55);
        Assert.assertTrue("Header 0x55", m1.getHeader() == 0x55);
    }

    @Test
    public void testRtrBit() {
        CanMessage m1 = new CanMessage(0x12);
        Assert.assertTrue("not rtr at start", !m1.isRtr());
        m1.setRtr(true);
        Assert.assertTrue("rtr set", m1.isRtr());
        m1.setRtr(false);
        Assert.assertTrue("rtr unset", !m1.isRtr());
    }

    @Test
    public void testStdExt() {
        CanMessage m1 = new CanMessage(0x12);
        Assert.assertTrue("std at start", !m1.isExtended());
        m1.setExtended(true);
        Assert.assertTrue("extended", m1.isExtended());
        m1.setExtended(false);
        Assert.assertTrue("std at end", !m1.isExtended());
    }

    @Test
    public void testDataElements() {
        CanMessage m1 = new CanMessage(0x12);

        m1.setNumDataElements(0);
        Assert.assertTrue("0 Elements", m1.getNumDataElements() == 0);

        m1.setNumDataElements(1);
        Assert.assertTrue("1 Elements", m1.getNumDataElements() == 1);

        m1.setNumDataElements(8);
        Assert.assertTrue("8 Elements", m1.getNumDataElements() == 8);

        m1.setNumDataElements(3);
        m1.setElement(0, 0x81);
        m1.setElement(1, 0x02);
        m1.setElement(2, 0x83);
        Assert.assertTrue("3 Elements", m1.getNumDataElements() == 3);
        Assert.assertTrue("3 Element 0", m1.getElement(0) == 0x81);
        Assert.assertTrue("3 Element 1", m1.getElement(1) == 0x02);
        Assert.assertTrue("3 Element 2", m1.getElement(2) == 0x83);
    }

    @Test
    @Override
    public void testToString() {
        CanMessage m1 = new CanMessage(0x12);
        m1.setNumDataElements(3);
        m1.setElement(0, 0x81);
        m1.setElement(1, 0x02);
        m1.setElement(2, 0x83);
        Assert.assertEquals("string representation", "[12] 81 02 83",m1.toString());
    }

    @Test
    @Override
    public void testToMonitorString() {
        CanMessage m1 = new CanMessage(0x12);
        m1.setNumDataElements(3);
        m1.setElement(0, 0x81);
        m1.setElement(1, 0x02);
        m1.setElement(2, 0x83);
        Assert.assertEquals("string representation", "(12) 81 02 83",m1.toMonitorString());
    }
    
    @Test
    public void testReplyExpected() {
        CanMessage m1 = new CanMessage(0x12);
        Assert.assertFalse("No Reply expected",m1.replyExpected());
    }
    
    @Test
    public void testSetData() {
        CanMessage m1 = new CanMessage(0x12);
        m1.setData( new int[]{1,2,3,4,5,6,7,8,9});
        Assert.assertEquals("data over frame length set ok", "(12) 01 02 03 04 05 06 07 08",m1.toMonitorString());
        m1.setData( new int[]{10,11,12});
        Assert.assertEquals("data under frame length set ok", "(12) 0A 0B 0C 04 05 06 07 08",m1.toMonitorString());
    }
    
    @Test
    public void testCreateFromLongArrays() {
        m = new CanMessage(new int[]{1,2,3,4,5,6,7,8,9},0x12);
        Assert.assertEquals("int data over frame length set ok", "(12) 01 02 03 04 05 06 07 08",m.toMonitorString());
        m = new CanMessage(new int[]{1,2,3},0x12);
        Assert.assertEquals("int data under frame length set ok", "(12) 01 02 03",m.toMonitorString());
        m = new CanMessage(new byte[]{(byte)0x10,(byte)0x122,(byte)0xbb,(byte)0xFF,(byte)0x129,
            (byte)0x255,(byte)0x09,(byte)0x99},0x12);
        Assert.assertEquals("byte data over frame length set ok", "(12) 10 22 BB FF 29 55 09 99",m.toMonitorString());
        m = new CanMessage(new byte[]{1,2,3,4,5,6,7,8,9},0x12);
        Assert.assertEquals("byte data over frame length set ok", "(12) 01 02 03 04 05 06 07 08",m.toMonitorString());
    }
    
    @Test
    public void testSetGetTranslated(){
        CanMessage m1 = new CanMessage(0x12);
        Assert.assertFalse("Not translated by default",m1.isTranslated());
        m1.setTranslated(true);
        Assert.assertTrue("translated flag set",m1.isTranslated());
    }

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        m = new CanMessage(0x12);
    }

    @AfterEach
    @Override
    public void tearDown() {
        m = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
