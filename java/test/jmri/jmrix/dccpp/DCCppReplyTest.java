package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppReply class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood (C) 2015
 */
public class DCCppReplyTest {

    @Test
    public void testCtor() {
        DCCppReply m = new DCCppReply();
        Assert.assertNotNull(m);
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        DCCppReply m = DCCppReply.parseDCCppReply("H 23 1");
        Assert.assertEquals("length", 6, m.getNumDataElements());
        Assert.assertEquals("0th byte", 'H', m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', m.getElement(5) & 0xFF);
    }

    // check is direct mode response
    @Test
    public void testIsDirectModeResponse() {
        // CV 1 in direct mode.
        DCCppReply r = DCCppReply.parseDCCppReply("r 1234|87|23 12");
        Assert.assertTrue(r.isProgramReply());
        r = DCCppReply.parseDCCppReply("r 1234|66|23 4 1");
        Assert.assertTrue(r.isProgramBitReply());
        r = DCCppReply.parseDCCppReply("r 1234|82|23 4");
        Assert.assertTrue(r.isProgramReply());
    }

    // check get service mode CV Number response code.
    @Test
    public void testGetServiceModeCVNumber() {
    }

    // check get service mode CV Value response code.
    @Test
    public void testGetServiceModeCVValue() {
    }
    
    // Test Comm Type Reply
    @Test
    public void testCommTypeReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("N0: SERIAL");
        Assert.assertTrue(l.isCommTypeReply());
        Assert.assertEquals('N', l.getOpCodeChar());
        Assert.assertEquals(0, l.getCommTypeInt());
        Assert.assertEquals("SERIAL", l.getCommTypeValueString());
        
        l = DCCppReply.parseDCCppReply("N1: 192.168.0.1");
        Assert.assertTrue(l.isCommTypeReply());
        Assert.assertEquals('N', l.getOpCodeChar());
        Assert.assertEquals(1, l.getCommTypeInt());
        Assert.assertEquals("192.168.0.1", l.getCommTypeValueString());
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
