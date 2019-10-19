package jmri.jmrix.can.cbus;

import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.TrafficControllerScaffoldLoopback;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 * testSendPacket modified from LocoNet SlotManagerTest
 */
public class CbusCommandStationTest {

    CbusCommandStation t;
    CanSystemConnectionMemo memo;
    TrafficControllerScaffold lnis;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testgetSystemPrefix() {
        Assert.assertEquals("sys prefix", "M", t.getSystemPrefix());
    }

    @Test
    public void testgetUserName() {
        Assert.assertEquals("user name obtainable", "CAN", t.getUserName());
    }
    
    @Test
    public void testgetSimLoopbacktc() {
        TrafficControllerScaffoldLoopback tc = new TrafficControllerScaffoldLoopback();
        memo.setTrafficController(tc);
        CbusCommandStation ta = new CbusCommandStation(memo);
        Assert.assertNotNull("exists",ta);
        Assert.assertNotNull(InstanceManager.getDefault(CbusSimulator.class));
        
        tc = null;
        ta = null;
    }

    // test originates from loconet
    @Test
    public void testSendPacket() {
        
        byte msg[] = jmri.NmraPacket.accDecPktOpsMode(1, 4, 53);
        t.sendPacket(msg, 1);
        Assert.assertEquals("nmra packet 1",
            "[78] E0 01 81 F0 EC 03 35 AB",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(128, 4, 53);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 2",
            "[78] E0 02 80 D0 EC 03 35 8A",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg= jmri.NmraPacket.accDecPktOpsMode(256, 4, 53);
        t.sendPacket(msg, 3);
        Assert.assertEquals("nmra packet 3",
            "[78] E0 03 80 B0 EC 03 35 EA",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 37, 53);
        t.sendPacket(msg, 4);
        Assert.assertEquals("nmra packet 4",
            "[78] E0 04 81 F0 EC 24 35 8C",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 129, 53);
        t.sendPacket(msg, 5);
        Assert.assertEquals("nmra packet 5",
            "[78] E0 05 81 F0 EC 80 35 28",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 10, 0);
        t.sendPacket(msg, 6);
        Assert.assertEquals("nmra packet 6",
            "[78] E0 06 81 F0 EC 09 00 94",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 10, 128);
        t.sendPacket(msg, 7);
        Assert.assertEquals("nmra packet 7",
            "[78] E0 07 81 F0 EC 09 80 14",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 10, 255);
        t.sendPacket(msg, 8);
        Assert.assertEquals("nmra packet 8",
            "[78] E0 08 81 F0 EC 09 FF 6B",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(511, 255, 0);
        t.sendPacket(msg, 9);
        jmri.util.JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' reduced to 8.");
        Assert.assertEquals("nmra packet 9",
            "[78] E0 08 BF 80 EC FE 00 2D",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(1, 31);
        t.sendPacket(msg, 0);
        jmri.util.JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' of < 1 is illegal and is forced to 1.");
        Assert.assertEquals("nmra packet 10",
            "[78] A0 01 81 71 1F EF",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(2, 30);
        t.sendPacket(msg, -1);
        jmri.util.JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' of < 1 is illegal and is forced to 1.");
        Assert.assertEquals("nmra packet 11",
            "[78] A0 01 81 73 1E EC",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(4, 29);
        t.sendPacket(msg, 3);
        Assert.assertEquals("nmra packet 12",
            "[78] A0 03 81 77 1D EB",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(8, 27);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 13",
            "[78] A0 02 82 77 1B EE",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(16, 23);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 14",
            "[78] A0 02 84 77 17 E4",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(32, 15);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 15",
            "[78] A0 02 88 77 0F F0",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(64, 1);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 16",
            "[78] A0 02 90 77 01 E6",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(128, 0);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 17",
            "[78] A0 02 A0 77 00 D7",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(256, 2);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 18",
            "[78] A0 02 80 67 02 E5",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(512, 4);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 19",
            "[78] A0 02 80 57 04 D3",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(1024, 8);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 20",
            "[78] A0 02 80 37 08 BF",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(511, 16);
        t.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 21",
            "[78] A0 02 80 55 10 C5",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
                
        lnis = null;
    }
    
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(lnis);
        t = new CbusCommandStation(memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        memo = null;
        t = null;
        lnis = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusCommandStationTest.class);

}
