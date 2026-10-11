package jmri.jmrix.lenz.liusbethernet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import jmri.jmrix.lenz.XNetPortControllerScaffold;

import org.junit.jupiter.api.*;

/**
 * <p>
 * Title: LIUSBEthernetXNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class LIUSBEthernetXNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    @Test
    @Override
    public void testOutbound() throws IOException {
        LIUSBEthernetXNetPacketizer c = (LIUSBEthernetXNetPacketizer)tc;
        // connect to iostream via port controller scaffold
        jmri.jmrix.lenz.XNetPortControllerScaffold p = new jmri.jmrix.lenz.XNetPortControllerScaffold();
        c.connectPort(p);
        jmri.jmrix.lenz.XNetMessage m = jmri.jmrix.lenz.XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendXNetMessage(m, null);

        p.flush();
        jmri.util.JUnitUtil.waitFor(()-> p.tostream.available()==6,"total length 6");

        assertEquals( 6, p.tostream.available(), "total length");
        assertEquals( 0xFF, p.tostream.readByte() & 0xff, "Header 0");
        assertEquals( 0xFE, p.tostream.readByte() & 0xff, "Header 1");
        assertEquals( 0x52, p.tostream.readByte() & 0xff, "Char 0");
        assertEquals( 0x05, p.tostream.readByte() & 0xff, "Char 1");
        assertEquals( 0x8A, p.tostream.readByte() & 0xff, "Char 2");
        assertEquals( 0xDD, p.tostream.readByte() & 0xff, "parity");
        assertEquals( 0, p.tostream.available(), "remaining");
    }

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new LIUSBEthernetXNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
        port = assertDoesNotThrow( () -> new XNetPortControllerScaffold(),
                "Error creating test port");
    }

}
