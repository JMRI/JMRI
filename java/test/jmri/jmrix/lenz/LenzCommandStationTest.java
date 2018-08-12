package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * LenzCommandStationTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.LenzCommandStation class
 *
 * @author	Paul Bender
 */
public class LenzCommandStationTest extends TestCase {

    public void testCtor() {

        LenzCommandStation c = new LenzCommandStation();
        Assert.assertNotNull(c);
    }

    public void testVersion() {
        // test setting the command station version from an XNetReply
        LenzCommandStation c = new LenzCommandStation();
        XNetReply r = new XNetReply();
        // test a version that is BCD
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36); // version 3.6
        r.setElement(3, 0x00);
        r.setElement(4, 0x74);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals(3.6f, c.getCommandStationSoftwareVersion(), 0.0);
        // test a version that is not BCD
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x8D); // version 8.13
        r.setElement(3, 0x00);
        r.setElement(4, 0xCF);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals(8.13f, c.getCommandStationSoftwareVersion(), 0.0);
        // make sure the command station version doesn't change when we send
        // the wrong message type.
        r = new XNetReply("01 04 05");
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals(8.13f, c.getCommandStationSoftwareVersion(), 0.0);
        // make sure the command station type doesn't change if we send
        // the right prefix (0x63) but a message that doesn't contain the
        // version (2nd byte is not 0x21).
        r = new XNetReply("63 10 05 0C 7A"); // this is a service mode response. 
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals(8.13f, c.getCommandStationSoftwareVersion(), 0.0);

    }

    public void testVersionBCD() {
        // test setting the command station version from an XNetReply
        LenzCommandStation c = new LenzCommandStation();
        XNetReply r = new XNetReply();
        // test a version that is BCD
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36); // version 3.6
        r.setElement(3, 0x00);
        r.setElement(4, 0x74);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals(0x36, c.getCommandStationSoftwareVersionBCD(), 0.0);
        // test a version that is not BCD
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x8D); // version 8.13
        r.setElement(3, 0x00);
        r.setElement(4, 0xCF);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals(0x8D, c.getCommandStationSoftwareVersionBCD(), 0.0);
    }

    public void testType() {
        // test setting the command station type from an XNetReply
        LenzCommandStation c = new LenzCommandStation();
        XNetReply r = new XNetReply();
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x00); // type is LZV100
        r.setElement(4, 0x74);
        c.setCommandStationType(r);
        Assert.assertEquals(0, c.getCommandStationType());
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x01); // type is LH200 
        r.setElement(4, 0x75);
        c.setCommandStationType(r);
        Assert.assertEquals(1, c.getCommandStationType());
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x02); // type is Compact 
        r.setElement(4, 0x76);
        c.setCommandStationType(r);
        Assert.assertEquals(2, c.getCommandStationType());
        // make sure the command station type doesn't change when we send 
        // the wrong message type.
        r = new XNetReply("01 04 05");
        c.setCommandStationType(r);
        Assert.assertEquals(2, c.getCommandStationType());
        // make sure the command station type doesn't change if we send
        // the right prefix (0x63) but a message that doesn't contain the
        // version (2nd byte is not 0x21).
        r = new XNetReply("63 10 05 0C 7A"); // this is a service mode response. 
        c.setCommandStationType(r);
        Assert.assertEquals(2, c.getCommandStationType());
    }

    public void testSetVersionFloat() {
        // test setting the command station version from using a numeric
        // value.
        LenzCommandStation c = new LenzCommandStation();
        c.setCommandStationSoftwareVersion(3.6f);
        Assert.assertEquals(3.6f, c.getCommandStationSoftwareVersion(), 0.0);
        c.setCommandStationSoftwareVersion(8.13f);
        Assert.assertEquals(8.13f, c.getCommandStationSoftwareVersion(), 0.0);
    }

    public void testSetTypeNumeric() {
        // test setting the command station type from using a numeric
        // value.
        LenzCommandStation c = new LenzCommandStation();
        c.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        Assert.assertEquals(XNetConstants.CS_TYPE_LZ100, c.getCommandStationType());
        c.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        Assert.assertEquals(XNetConstants.CS_TYPE_LH200, c.getCommandStationType());
        c.setCommandStationType(XNetConstants.CS_TYPE_COMPACT);
        Assert.assertEquals(XNetConstants.CS_TYPE_COMPACT, c.getCommandStationType());
        c.setCommandStationType(XNetConstants.CS_TYPE_MULTIMAUS);
        Assert.assertEquals(XNetConstants.CS_TYPE_MULTIMAUS, c.getCommandStationType());
    }

    public void testGetVersionString() {
        // test getting the command station version string.
        LenzCommandStation c = new LenzCommandStation();
        XNetReply r = new XNetReply();
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x00); // type is LZV100
        r.setElement(4, 0x74);
        c.setCommandStationType(r);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals("hardware type: 0 software version: 54",c.getVersionString());
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x01); // type is LH200 
        r.setElement(4, 0x75);
        c.setCommandStationType(r);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals("hardware type: 1 software version: 54",c.getVersionString());
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x02); // type is Compact 
        r.setElement(4, 0x76);
        c.setCommandStationType(r);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertEquals("hardware type: 2 software version: 54",c.getVersionString());
    }

    public void testIsOpsModePossible() {
        // test getting the command station version string.
        LenzCommandStation c = new LenzCommandStation();
        XNetReply r = new XNetReply();
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x00); // type is LZV100
        r.setElement(4, 0x74);
        c.setCommandStationType(r);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertTrue(c.isOpsModePossible());
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x01); // type is LH200 
        r.setElement(4, 0x75);
        c.setCommandStationType(r);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertFalse(c.isOpsModePossible());
        r.setElement(0, 0x63);
        r.setElement(1, 0x21);
        r.setElement(2, 0x36);
        r.setElement(3, 0x02); // type is Compact 
        r.setElement(4, 0x76);
        c.setCommandStationType(r);
        c.setCommandStationSoftwareVersion(r);
        Assert.assertFalse(c.isOpsModePossible());
   } 


    public void testGetDCCAddressLow() {
        Assert.assertEquals(0x42,LenzCommandStation.getDCCAddressLow(0x0042));
        Assert.assertEquals(0x42,LenzCommandStation.getDCCAddressLow(0x1042));
    }

    public void testGetDCCAddressHigh() {
        Assert.assertEquals(0x00,LenzCommandStation.getDCCAddressHigh(0x0042));
        Assert.assertEquals(0xD0,LenzCommandStation.getDCCAddressHigh(0x1042));
    }
  
    public void testGetUserName() {
        LenzCommandStation c = new LenzCommandStation();
        Assert.assertEquals("XpressNet",c.getUserName()); // default.
        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(new XNetInterfaceScaffold(c));
        c.setSystemConnectionMemo(memo);
        memo.setUserName("ABC");
        Assert.assertEquals("ABC",c.getUserName());
    }

    public void testGetSystemPrefix() {
        LenzCommandStation c = new LenzCommandStation();
        Assert.assertEquals("X",c.getSystemPrefix()); // default.
        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(new XNetInterfaceScaffold(c));
        c.setSystemConnectionMemo(memo);
        memo.setSystemPrefix("ABC");
        Assert.assertEquals("ABC",c.getSystemPrefix());
    }

    public void testSendPacket() {
        LenzCommandStation c = new LenzCommandStation();
        // sending without setting the traffic controller should
        // generate an error message.
        c.sendPacket(jmri.NmraPacket.opsCvWriteByte(100,true,29,5),1);
jmri.util.JUnitAppender.assertErrorMessage("Send Packet Called without setting traffic controller");

        XNetInterfaceScaffold xis = new XNetInterfaceScaffold(c);
        c.setTrafficController(xis);
        c.sendPacket(jmri.NmraPacket.opsCvWriteByte(100,true,29,5),1);

        Assert.assertEquals(1,xis.outbound.size());
        Assert.assertEquals("packet message contents", "E6 30 C0 64 EC 1C 05 87", xis.outbound.elementAt(0).toString());
    }

    // from here down is testing infrastructure
    public LenzCommandStationTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LenzCommandStationTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LenzCommandStationTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
