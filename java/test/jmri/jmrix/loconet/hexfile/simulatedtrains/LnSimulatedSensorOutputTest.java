package jmri.jmrix.loconet.hexfile.simulatedtrains;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link LnSimulatedSensorOutput}'s sensor input reports, checked
 * byte by byte against the LocoNet Personal Edition's format: 0xB2, IN1 =
 * A6..A0, IN2 = 0 X I L A10..A7, checksum.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LnSimulatedSensorOutputTest {

    private static String hex(LocoNetMessage m) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < m.getNumDataElements(); i++) {
            b.append(i == 0 ? "" : " ").append(String.format("%02X", m.getElement(i)));
        }
        return b.toString();
    }

    @Test
    public void testInputReports() {
        // LS1: input 0, A = 0, I = 0; occupied: L = 1, X = 1
        assertEquals("B2 00 50 1D", hex(LnSimulatedSensorOutput.inputReport(0, true)));
        assertEquals("B2 00 40 0D", hex(LnSimulatedSensorOutput.inputReport(0, false)));
        // LS2: input 1, I = 1
        assertEquals("B2 00 70 3D", hex(LnSimulatedSensorOutput.inputReport(1, true)));
        // LS257: input 256, A = 128: A6..A0 = 0, A10..A7 = 1
        assertEquals("B2 00 51 1C", hex(LnSimulatedSensorOutput.inputReport(256, true)));
        // LS4096: input 4095, A = 2047: A6..A0 = 0x7F, A10..A7 = 0xF, I = 1
        assertEquals("B2 7F 7F 4D", hex(LnSimulatedSensorOutput.inputReport(4095, true)));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
