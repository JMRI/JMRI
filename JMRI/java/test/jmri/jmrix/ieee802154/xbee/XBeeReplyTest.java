package jmri.jmrix.ieee802154.xbee;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * XBeeReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeReply class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeReplyTest {

    @Test
    public void testCtor() {
        XBeeReply m = new XBeeReply();
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testStringCtor(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testCopyCtor(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        XBeeReply c = new XBeeReply(m);
        Assert.assertNotNull("exists", c);
    }

    @Test
    public void testXBeeResponseCtor(){
        String s = new String("000A8300072B00010011001028");
        byte ba[] = jmri.util.StringUtil.bytesFromHexString(s);
        com.digi.xbee.api.packet.UnknownXBeePacket xbresponse = com.digi.xbee.api.packet.UnknownXBeePacket.createPacket(ba);
        XBeeReply m = new XBeeReply(xbresponse);
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testToString(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        // the Digi XBee API method of doing this adds readio
        // header and trailer information to the ouptut.
        Assert.assertEquals("ToString Return","7E000D000A8300072B00010011001028F6",
                          m.toString());
    }

    @Test
    public void testToMonitorString(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        // since we're letting the XBee API generate the monitor string, just 
        // check to make sure the monitor string is not null.
        Assert.assertNotNull("ToMonitorString Return",m.toMonitorString());
    }

    @Test
    public void testGetXBeeResponse(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        // make sure the XBeeResponse is not null.
        Assert.assertNotNull("getXBeeResponse return",m.getXBeeResponse());
    }

    @Test
    public void testSetXBeeResponse(){
        XBeeReply m = new XBeeReply();
        String s = new String("000A8300072B00010011001028");
        byte ba[] = jmri.util.StringUtil.bytesFromHexString(s);
        com.digi.xbee.api.packet.UnknownXBeePacket xbresponse = com.digi.xbee.api.packet.UnknownXBeePacket.createPacket(ba);
       
        m.setXBeeResponse(xbresponse);
 
        // make sure the XBeeResponse is not null.
        Assert.assertNotNull("getXBeeRsponse after Set",m.getXBeeResponse());
        Assert.assertEquals("xbee response after set",xbresponse,m.getXBeeResponse());
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
