package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the DccSignalHead implementation
 *
 * @author Bob Jacobsen Copyright (C) 2013
 */
public class DccSignalHeadTest extends AbstractSignalHeadTestBase {

    @Test
    public void testDccSignalHeadCtor1() {
        DccSignalHead s = new DccSignalHead("IH$1");

        assertEquals( "IH$1", s.getSystemName(), "system name");
        assertEquals( 0, sentPacketCount, "Send count");
    }

    @Test
    public void testRedAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.RED);

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x00, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    @Test
    public void testDarkAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.RED);  // Default is DARK
        s.setAppearance(SignalHead.DARK);

        assertEquals( 2, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x08, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    @Test
    public void testLunarAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.LUNAR);

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x03, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    @Test
    public void testYellowAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.YELLOW);

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x01, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    @Test
    public void testGreenAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.GREEN);

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x02, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    @Test
    public void testFlashRedAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHRED);

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x04, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    @Test
    public void testFlashLunarAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHLUNAR);

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x07, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    @Test
    public void testFlashYellowAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHYELLOW);

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x05, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    @Test
    public void testFlashGreenAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHGREEN);

        assertEquals( 1, sentPacketCount, "Send count");
        assertEquals( 4, lastSentPacket.length, "Packet length");
        assertEquals( 0x80, lastSentPacket[0] & 0xFF, "Packet byte 0");
        assertEquals( 0x71, lastSentPacket[1] & 0xFF, "Packet byte 1");
        assertEquals( 0x06, lastSentPacket[2] & 0xFF, "Packet byte 2");

    }

    // from here down is testing infrastructure

    @Override
    public SignalHead getHeadToTest() {
        return new DccSignalHead("IH$1");
    }
    
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
