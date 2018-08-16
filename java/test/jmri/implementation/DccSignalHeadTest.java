package jmri.implementation;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DccSignalHead implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2013
 */
public class DccSignalHeadTest extends AbstractSignalHeadTestBase {

    @Test
    public void testCtor1() {
        DccSignalHead s = new DccSignalHead("IH$1");

        Assert.assertEquals("system name", "IH$1", s.getSystemName());
        Assert.assertEquals("Send count", 0, sentPacketCount);
    }

    @Test
    public void testRedAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.RED);

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x00, lastSentPacket[2] & 0xFF);

    }

    @Test
    public void testDarkAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.RED);  // Default is DARK
        s.setAppearance(SignalHead.DARK);

        Assert.assertEquals("Send count", 2, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x08, lastSentPacket[2] & 0xFF);

    }

    @Test
    public void testLunarAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.LUNAR);

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x03, lastSentPacket[2] & 0xFF);

    }

    @Test
    public void testYellowAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.YELLOW);

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x01, lastSentPacket[2] & 0xFF);

    }

    @Test
    public void testGreenAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.GREEN);

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x02, lastSentPacket[2] & 0xFF);

    }

    @Test
    public void testFlashRedAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHRED);

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x04, lastSentPacket[2] & 0xFF);

    }

    @Test
    public void testFlashLunarAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHLUNAR);

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x07, lastSentPacket[2] & 0xFF);

    }

    @Test
    public void testFlashYellowAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHYELLOW);

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x05, lastSentPacket[2] & 0xFF);

    }

    @Test
    public void testFlashGreenAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHGREEN);

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x06, lastSentPacket[2] & 0xFF);

    }

    // from here down is testing infrastructure

    @Override
    public SignalHead getHeadToTest() {
        return new DccSignalHead("IH$1");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();

        CommandStation c = new CommandStation() {
            @Override
            public boolean sendPacket(byte[] packet, int repeats) {
                lastSentPacket = packet;
                sentPacketCount++;
                return true;
            }

            @Override
            public String getUserName() {
                return null;
            }

            @Override
            public String getSystemPrefix() {
                return "I";
            }
        };
        InstanceManager.store(c, CommandStation.class);
        lastSentPacket = null;
        sentPacketCount = 0;
    }
    byte[] lastSentPacket;
    int sentPacketCount;

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
