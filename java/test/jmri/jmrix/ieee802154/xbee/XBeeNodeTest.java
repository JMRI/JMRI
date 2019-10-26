package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeProtocol;
import org.junit.*;

/**
 * XBeeNodeTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeNode class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XBeeNodeTest{

    private XBeeTrafficController tc = null;

    @Test
    public void testCtor() {
        XBeeNode m = new XBeeNode();
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testCtorWithParamters() {
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        node.setTrafficController(tc);
        Assert.assertNotNull("exists", node);
        Assert.assertEquals("Node PAN address high byte", pan[0], node.getPANAddress()[0]);
        Assert.assertEquals("Node PAN address low byte", pan[1], node.getPANAddress()[1]);
        Assert.assertEquals("Node user address high byte", uad[0], node.getUserAddress()[0]);
        Assert.assertEquals("Node user address low byte", uad[1], node.getUserAddress()[1]);
        for (int i = 0; i < gad.length; i++) {
            Assert.assertEquals("Node global address byte " + i, gad[i], node.getGlobalAddress()[i]);
        }
    }

    @Test
    public void testSetPANAddress() {
        // test the code to set the User address
        XBeeNode node = new XBeeNode();
        byte pan[] = {(byte) 0x00, (byte) 0x01};
        node.setPANAddress(pan);
        Assert.assertEquals("Node PAN address high byte", pan[0], node.getPANAddress()[0]);
        Assert.assertEquals("Node PAN address low byte", pan[1], node.getPANAddress()[1]);
    }

    @Test
    public void testSetUserAddress() {
        // test the code to set the User address
        XBeeNode node = new XBeeNode();
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        Assert.assertEquals("Node user address high byte", uad[0], node.getUserAddress()[0]);
        Assert.assertEquals("Node user address low byte", uad[1], node.getUserAddress()[1]);
    }

    @Test
    public void testSetGlobalAddress() {
        // test the code to set the User address
        XBeeNode node = new XBeeNode();
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        for (int i = 0; i < gad.length; i++) {
            Assert.assertEquals("Node global address byte " + i, gad[i], node.getGlobalAddress()[i]);
        }
    }

    @Test
    public void testGetPreferedNameAsUserAddress() {
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("6D97"),
             "");
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        node.setXBee(rd);
        tc.registerNode(node);
        Assert.assertEquals("Short Address Name","6D 97 ",node.getPreferedName());
    }

    @Test
    public void testGetPreferedNameAsGlobalAddress() {
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("FFFE"),
             "");
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0xFF, (byte) 0xFE};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        tc.registerNode(node);
        node.setXBee(rd);
        Assert.assertEquals("Global Address Name","00 13 A2 00 40 A0 4D 2D ",node.getPreferedName());
    }

    @Test
    public void testGetPreferedNameAsNodeIdentifier() {
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("FFFF"),
             "Hello World");
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0xFF, (byte) 0xFF};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        node.setXBee(rd);
        node.setIdentifier("Hello World");
        tc.registerNode(node);
        Assert.assertEquals("Identifier Name",node.getPreferedName(),"Hello World");
    }

    @Test
    public void testGetPreferedTransmitUserAddress() {
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("6D97"),
             "");
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        node.setXBee(rd);
        tc.registerNode(node);
        Assert.assertEquals("Short Transmit Address",node.getXBeeAddress16(),node.getPreferedTransmitAddress());
    }

    @Test
    public void testGetPreferedTransmitGlobalAddress() {
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("FFFF"),
             "");
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0xFF, (byte) 0xFF};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        node.setXBee(rd);
        tc.registerNode(node);
        Assert.assertEquals("Global Transmit Address",node.getXBeeAddress64(),node.getPreferedTransmitAddress());
    }

    @Test
    public void testGetPreferedTransmitGlobalAddressWithMaskRequired() {
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("FFFF"),
             "");
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x0fffffff, (byte) 0x0ffffffe};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        node.setXBee(rd);
        tc.registerNode(node);
        Assert.assertEquals("Global Transmit Address",node.getXBeeAddress64(),node.getPreferedTransmitAddress());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
    }

    @After
    public void tearDown() {
        ((XBeeInterfaceScaffold)tc).dispose();
        tc = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();

    }

}
