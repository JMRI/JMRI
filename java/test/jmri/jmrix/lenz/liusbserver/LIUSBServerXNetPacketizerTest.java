package jmri.jmrix.lenz.liusbserver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import jmri.jmrix.lenz.XNetPortControllerScaffold;

import org.junit.jupiter.api.*;

/**
 * <p>
 * Title: LIUSBServerXNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class LIUSBServerXNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    @Test
    @Override
    public void testOutbound() throws IOException {
        LIUSBServerXNetPacketizer c = (LIUSBServerXNetPacketizer)tc;
        // connect to iostream via port controller scaffold
        jmri.jmrix.lenz.XNetPortControllerScaffold p = new jmri.jmrix.lenz.XNetPortControllerScaffold();
        c.connectPort(p);
        jmri.jmrix.lenz.XNetMessage m = jmri.jmrix.lenz.XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendXNetMessage(m, null);

        p.flush();
        jmri.util.JUnitUtil.waitFor(()-> p.tostream.available()==13,"total length 13");

        assertEquals( 13, p.tostream.available(), "total length");
        assertEquals( '5', p.tostream.readByte() & 0xff, "Char 0");
        assertEquals( '2', p.tostream.readByte() & 0xff, "Char 1");
        assertEquals( ' ', p.tostream.readByte() & 0xff, "Char 2");
        assertEquals( '0', p.tostream.readByte() & 0xff, "Char 3");
        assertEquals( '5', p.tostream.readByte() & 0xff, "Char 4");
        assertEquals( ' ', p.tostream.readByte() & 0xff, "Char 5");
        assertEquals( '8', p.tostream.readByte() & 0xff, "Char 6");
        assertEquals( 'A', p.tostream.readByte() & 0xff, "Char 7");
        assertEquals( ' ', p.tostream.readByte() & 0xff, "Char 8");
        assertEquals( 'D', p.tostream.readByte() & 0xff, "Char 9");
        assertEquals( 'D', p.tostream.readByte() & 0xff, "Char 10");
        assertEquals( '\n', p.tostream.readByte() & 0xff, "Char 11");
        assertEquals( '\r', p.tostream.readByte() & 0xff, "Char 12");
        assertEquals( 0, p.tostream.available(), "remaining");
    }

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new LIUSBServerXNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
        port = assertDoesNotThrow( () -> new XNetPortControllerScaffold(),
            "Error creating test port");
    }

}
