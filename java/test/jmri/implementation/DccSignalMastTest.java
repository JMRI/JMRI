package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the DccSignalMast implementation
 *
 * @author Bob Jacobsen Copyright (C) 2013
 * updated to JUnit4 2016
 */
public class DccSignalMastTest {

    @Test
    public void testCtor1() {
        DccSignalMast s = new DccSignalMast("IF$dsm:AAR-1946:PL-1-high-abs(1)");

        assertEquals( "IF$dsm:AAR-1946:PL-1-high-abs(1)", s.getSystemName(), "system name");
        assertEquals( 0, sentPacketCount, "Send count");
        assertFalse( s.useAddressOffSet(), "Use address offset");
    }

    @Test
    public void testStopAspect() {
        DccSignalMast s = new DccSignalMast("IF$dsm:AAR-1946:PL-1-high-abs(1)");
        s.setOutputForAppearance("Stop", 31);

        s.setAspect("Stop");

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x1F, lastSentPacket[2] & 0xFF, "Packet byte 2");
        assertEquals( 0xEE, lastSentPacket[3] & 0xFF, "Packet byte 3");

    }

    // from here down is testing infrastructure

    @BeforeEach
    public void setUp() {
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

    private byte[] lastSentPacket;
    private int sentPacketCount;

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
