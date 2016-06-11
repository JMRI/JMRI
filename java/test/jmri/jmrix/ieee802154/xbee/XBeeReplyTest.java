package jmri.jmrix.ieee802154.xbee;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XBeeReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeReply class
 *
 * @author	Paul Bender
 */
public class XBeeReplyTest extends TestCase {

    public void testCtor() {
        XBeeReply m = new XBeeReply();
        Assert.assertNotNull("exists", m);
    }

    public void testStringCtor(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        Assert.assertNotNull("exists", m);
    }

    public void testCopyCtor(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        XBeeReply c = new XBeeReply(m);
        Assert.assertNotNull("exists", c);
    }

    public void testXBeeResponseCtor(){
        String s = new String("000A8300072B00010011001028");
        com.rapplogic.xbee.api.GenericResponse xbresponse = new com.rapplogic.xbee.api.GenericResponse();
        byte ba[] = jmri.util.StringUtil.bytesFromHexString(s);
        int ia[] = new int[ba.length];
        for (int i = 0; i < ba.length; i++) {
            ia[i] = ba[i];
        }
        xbresponse.setRawPacketBytes(ia);

        XBeeReply m = new XBeeReply(xbresponse);
        Assert.assertNotNull("exists", m);
    }

    public void testToString(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        Assert.assertTrue("ToString Return",
                          m.toString().equals("000A8300072B00010011001028"));
    }

    public void testToMonitorString(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        // since we're letting the XBee API generate the monitor string, just 
        // check to make sure the monitor string is not null.
        Assert.assertNotNull("ToMonitorString Return",m.toMonitorString());
    }

    public void testGetXBeeResponse(){
        XBeeReply m = new XBeeReply("000A8300072B00010011001028");
        // make sure the XBeeResponse is not null.
        Assert.assertNotNull("getXBeeResponse return",m.getXBeeResponse());
    }

    public void testSetXBeeResponse(){
        XBeeReply m = new XBeeReply();
        String s = new String("000A8300072B00010011001028");
        com.rapplogic.xbee.api.GenericResponse xbresponse = new com.rapplogic.xbee.api.GenericResponse();
        byte ba[] = jmri.util.StringUtil.bytesFromHexString(s);
        int ia[] = new int[ba.length];
        for (int i = 0; i < ba.length; i++) {
            ia[i] = ba[i];
        }
        xbresponse.setRawPacketBytes(ia);
       
        m.setXBeeResponse(xbresponse);
 
        // make sure the XBeeResponse is not null.
        Assert.assertNotNull("getXBeeRsponse after Set",m.getXBeeResponse());
        Assert.assertEquals("xbee response after set",xbresponse,m.getXBeeResponse());
    }

    // from here down is testing infrastructure
    public XBeeReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XBeeReplyTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XBeeReplyTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
