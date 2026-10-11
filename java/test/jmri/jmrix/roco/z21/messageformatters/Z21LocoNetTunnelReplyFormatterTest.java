package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.*;

public class Z21LocoNetTunnelReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testMonitorStringLocoNetReply() {

        byte msg[] = {(byte) 0x11, (byte) 0x00, (byte) 0xA2, (byte) 0x00,
                (byte) 0xEF, (byte) 0x0E, (byte) 0x03, (byte) 0x00, (byte) 0x03,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply message = new Z21Reply(msg, 17);
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals( "LocoNet Tunnel Reply: Write slot 3 information:\n"
                + "\tLoco 3 (short) is Not Consisted, Free, operating in 28 SS mode, and is moving Forward at speed 0,\n"
                + "\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                + "\tMaster supports DT200; Track Status: Off/Paused; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n", formatter.formatMessage(message));
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21LocoNetTunnelReplyFormatter();
    }

}
