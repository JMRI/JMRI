package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21LocoNetTunnelRequestFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21LocoNetTunnelRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testMonitorStringLocoNetMessage() {
        byte message[] = {
                (byte) 0xEF, (byte) 0x0E, (byte) 0x03, (byte) 0x00, (byte) 0x03,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00};
        jmri.jmrix.loconet.LocoNetMessage l = new jmri.jmrix.loconet.LocoNetMessage(message);
        Z21Message msg = new Z21Message(l);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("LocoNet Tunnel Message: Write slot 3 information:\n\tLoco 3 (short) is Not Consisted, Free, operating in 28 SS mode, and is moving Forward at speed 0,\n\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n\tMaster supports DT200; Track Status: Off/Paused; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n", formatter.formatMessage(msg));
    }

    @Test
    public void testMonitorStringLocoNetMessage2() {
        byte message[] = {
                (byte) 0xD0, (byte) 0x20, (byte) 0x04,
                (byte) 0x7D, (byte) 0x0A, (byte) 0x7C};
        jmri.jmrix.loconet.LocoNetMessage l = new jmri.jmrix.loconet.LocoNetMessage(message);
        Z21Message msg = new Z21Message(l);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("LocoNet Tunnel Message: Transponder address 10 (short) (or long address 16010) present at LR5 () (BDL16x Board ID 1 RX4 zone C or BXP88 Board ID 1 section 5 or the BXPA1 Board ID 5 section).\n", formatter.formatMessage(msg));
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21LocoNetTunnelRequestFormatter();
    }

}
