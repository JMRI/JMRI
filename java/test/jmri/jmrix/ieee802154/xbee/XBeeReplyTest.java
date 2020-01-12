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
public class XBeeReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    @Test
    public void testStringCtor(){
        m = new XBeeReply("000A8300072B00010011001028");
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testCopyCtor(){
        XBeeReply msg = new XBeeReply("000A8300072B00010011001028");
        XBeeReply c = new XBeeReply(msg);
        Assert.assertNotNull("exists", c);
    }

    @Test
    public void testXBeeResponseCtor(){
        String s = new String("000A8300072B00010011001028");
        byte ba[] = jmri.util.StringUtil.bytesFromHexString(s);
        com.digi.xbee.api.packet.UnknownXBeePacket xbresponse = com.digi.xbee.api.packet.UnknownXBeePacket.createPacket(ba);
        m = new XBeeReply(xbresponse);
        Assert.assertNotNull("exists", m);
    }

    @Test
    @Override
    public void testToString(){
        XBeeReply msg = new XBeeReply("000A8300072B00010011001028");
        // the Digi XBee API method of doing this adds readio
        // header and trailer information to the ouptut.
        Assert.assertEquals("ToString Return","7E000D000A8300072B00010011001028F6",
                          msg.toString());
    }

    @Test
    @Override
    public void testToMonitorString(){
        XBeeReply msg = new XBeeReply("000A8300072B00010011001028");
        // since we're letting the XBee API generate the monitor string, just 
        // check to make sure the monitor string is not null.
        Assert.assertNotNull("ToMonitorString Return",msg.toMonitorString());
    }

    @Test
    public void testGetXBeeResponse(){
        XBeeReply msg = new XBeeReply("000A8300072B00010011001028");
        // make sure the XBeeResponse is not null.
        Assert.assertNotNull("getXBeeResponse return",msg.getXBeeResponse());
    }

    @Test
    public void testSetXBeeResponse(){
        XBeeReply msg = new XBeeReply();
        String s = new String("000A8300072B00010011001028");
        byte ba[] = jmri.util.StringUtil.bytesFromHexString(s);
        com.digi.xbee.api.packet.UnknownXBeePacket xbresponse = com.digi.xbee.api.packet.UnknownXBeePacket.createPacket(ba);
       
        msg.setXBeeResponse(xbresponse);
 
        // make sure the XBeeResponse is not null.
        Assert.assertNotNull("getXBeeRsponse after Set",msg.getXBeeResponse());
        Assert.assertEquals("xbee response after set",xbresponse,msg.getXBeeResponse());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new XBeeReply();
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

}
