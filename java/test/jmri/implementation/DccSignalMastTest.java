package jmri.implementation;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DccSignalMast implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2013
 * updated to JUnit4 2016
 */
public class DccSignalMastTest {

    @Test
    public void testCtor1() {
        DccSignalMast s = new DccSignalMast("IF$dsm:AAR-1946:PL-1-high-abs(1)");

        Assert.assertEquals("system name", "IF$dsm:AAR-1946:PL-1-high-abs(1)", s.getSystemName());
        Assert.assertEquals("Send count", 0, sentPacketCount);
    }

    @Test
    public void testStopAspect() {
        DccSignalMast s = new DccSignalMast("IF$dsm:AAR-1946:PL-1-high-abs(1)");
        s.setOutputForAppearance("Stop", 31);

        s.setAspect("Stop");

        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0] & 0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1] & 0xFF);
        Assert.assertEquals("Packet byte 2", 0x1F, lastSentPacket[2] & 0xFF);
        Assert.assertEquals("Packet byte 3", 0xEE, lastSentPacket[3] & 0xFF);

    }

    // from here down is testing infrastructure

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
