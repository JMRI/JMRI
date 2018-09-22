package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * DCCppReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppReply class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood (C) 2015
 */
public class DCCppReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private DCCppReply msg = null;

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        msg = DCCppReply.parseDCCppReply("H 23 1");
        Assert.assertEquals("length", 6, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 'H', msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", ' ', msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", '2', msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", '3', msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", ' ', msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", '1', msg.getElement(5) & 0xFF);
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
    @Ignore("Method is not implemented")
    public void testGetServiceModeCVNumber() {
    }

    // check get service mode CV Value response code.
    @Test
    @Ignore("Method is not implemented")
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

    // Test named power districts
    @Test
    public void testNamedPowerDistrictReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("p 0 MAIN");
        Assert.assertTrue(l.isNamedPowerReply());
        Assert.assertEquals('p', l.getOpCodeChar());
        Assert.assertEquals("MAIN", l.getPowerDistrictName());
        Assert.assertEquals("OFF", l.getPowerDistrictStatus());

        l = DCCppReply.parseDCCppReply("p 1 MAIN");
        Assert.assertTrue(l.isNamedPowerReply());
        Assert.assertEquals('p', l.getOpCodeChar());
        Assert.assertEquals("MAIN", l.getPowerDistrictName());
        Assert.assertEquals("ON", l.getPowerDistrictStatus());

        l = DCCppReply.parseDCCppReply("p 2 MAIN");
        Assert.assertTrue(l.isNamedPowerReply());
        Assert.assertEquals('p', l.getOpCodeChar());
        Assert.assertEquals("MAIN", l.getPowerDistrictName());
        Assert.assertEquals("OVERLOAD", l.getPowerDistrictStatus());
    }

    // Test named power districts
    @Test
    public void testNamedCurrentReply() {
        DCCppReply l = DCCppReply.parseDCCppReply("a MAIN 0");
        Assert.assertTrue(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("0", l.getCurrentString());

        l = DCCppReply.parseDCCppReply("a MAIN 100");
        Assert.assertTrue(l.isNamedCurrentReply());
        Assert.assertEquals('a', l.getOpCodeChar());
        Assert.assertEquals("100", l.getCurrentString());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new DCCppReply();
    }

    @After
    public void tearDown() {
	m = msg = null;
        JUnitUtil.tearDown();
    }

}
