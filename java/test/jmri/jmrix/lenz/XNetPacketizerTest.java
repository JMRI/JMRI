package jmri.jmrix.lenz;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.XNetPacketizer package
 *
 * @author Bob Jacobsen Copyrgiht (C) 2002
 * @author Paul Bender Copyright (C) 2016
 */
public class XNetPacketizerTest extends XNetTrafficControllerTest {
        
    protected XNetPortControllerScaffold port = null;

    /**
     * Local test class to make XNetPacketizer more felicitous to test
     */
    static class StoppingXNetPacketizer extends XNetPacketizer {

        StoppingXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation p) {
            super(p);
        }

        // methods removed for testing
        @Override
        protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
        } // don't care about timeout

        @Override
        protected void reportReceiveLoopException(Exception e) {
        }

        @Override
        protected void portWarn(Exception e) {
        }
    }

    @Test
    public void testOutbound() throws IOException {
        XNetPacketizer c = (XNetPacketizer) tc;
        // connect to iostream via port controller scaffold
        tc.connectPort(port);
        XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendXNetMessage(m, null);

        port.flush();
        JUnitUtil.waitFor(() -> port.tostream.available() == 4, "total length 4");

        assertEquals( 4, port.tostream.available(), "total length ");
        assertEquals( 0x52, port.tostream.readByte() & 0xff, "Char 0");
        assertEquals( 0x05, port.tostream.readByte() & 0xff, "Char 1");
        assertEquals( 0x8A, port.tostream.readByte() & 0xff, "Char 2");
        assertEquals( 0xDD, port.tostream.readByte() & 0xff, "parity");
        assertEquals( 0, port.tostream.available(), "remaining ");
    }

    @Test
    public void testInbound() throws IOException {
        XNetPacketizer c = (XNetPacketizer) tc;
        // connect to iostream via port controller scaffold
        tc.connectPort(port);

        // object to receive reply
        XNetListenerScaffold l = new XNetListenerScaffold();
        c.addXNetListener(~0, l);

        // now send reply
        port.tistream.write(0x52);
        port.tistream.write(0x12);
        port.tistream.write(0x12);
        port.tistream.write(0x52);

        port.flush();
        JUnitUtil.waitFor(() -> l.rcvdRply != null, "reply received");

        // check that the message was picked up by the read thread.
        assertNotNull( l.rcvdRply, "reply received ");
        assertEquals( 0x52, l.rcvdRply.getElement(0), "first char of reply ");
    }

    @Test
    public void testInterference() throws IOException {
        // This test checks to make sure that when two listeners register for events
        // at the same time, the first listener is still the active listener until
        // it receives a message.
        XNetPacketizer c = (XNetPacketizer) tc;
        // connect to iostream via port controller scaffold
        tc.connectPort(port);

        // We need three objects to receive messages.
        // The first one receives broadcast messages.
        // The others only receive directed messages.
        XNetListenerScaffold l = new XNetListenerScaffold();
        XNetListenerScaffold l1 = new XNetListenerScaffold();
        XNetListenerScaffold l2 = new XNetListenerScaffold();
        c.addXNetListener(~0, l);

        // we're going to loop through this, because we're trying to catch
        // a threading/synchronization issue in AbstractMRTrafficController.
        for (int i = 0; i < 5; i++) {

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            // first, we send an unsolicited message
            port.tistream.write(0x42);
            port.tistream.write(0x12);
            port.tistream.write(0x12);
            port.tistream.write(0x42);

            port.flush();
            JUnitUtil.waitFor(() -> l.rcvdRply != null, "reply received");

            // check that the message was picked up by the read thread.
            assertNotNull( l.rcvdRply, "iteration " + i + " reply received ");
            assertEquals( 0x42, l.rcvdRply.getElement(0), "iteration " + i + " first char of broadcast reply to l");

            // now we need to send a message with both the second and third listeners
            // as reply receiver.
            XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
            c.sendXNetMessage(m, l1);

            XNetMessage m1 = XNetMessage.getTurnoutCommandMsg(23, true, false, true);
            c.sendXNetMessage(m1, l2);

            port.flush();

            // and now we verify l1 is the last sender.
            JUnitUtil.waitFor(() -> l1 == c.getLastSender(), "iteration " + i + " Last Sender l1, before l1 reply");

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            // Now we reply to the messages above
            port.tistream.write(0x01);
            port.tistream.write(0x04);
            port.tistream.write(0x05);

            port.flush();
            JUnitUtil.waitFor(() -> l1.rcvdRply != null, "iteration " + i + " reply received ");

            // check that the message was picked up by the read thread.
            assertNotNull( l1.rcvdRply, "iteration " + i + " reply received ");
            assertEquals( 0x01, l1.rcvdRply.getElement(0), "iteration " + i + " first char of reply to l1");

            assertNotNull( l.rcvdRply, "iteration " + i + " broadcast reply after l1 message");
            assertNotNull( l1.rcvdRply, "iteration " + i + " l1 reply after l1 message");
            assertNull( l2.rcvdRply, "iteration " + i + " l2 reply after l1 message");

            // and now we verify l2 is the last sender.
            JUnitUtil.waitFor(() -> l2 == c.getLastSender(), "Last Sender l2");

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            port.tistream.write(0x01);
            port.tistream.write(0x04);
            port.tistream.write(0x05);

            port.flush();
            JUnitUtil.waitFor(() -> l2.rcvdRply != null, "iteration " + i + " reply received ");

            // check that the message was picked up by the read thread.
            assertNotNull( l2.rcvdRply, "iteration " + i + " reply received ");

            assertEquals( 0x01, l2.rcvdRply.getElement(0), "iteration " + i + " first char of reply to l2");

            assertNotNull( l.rcvdRply, "iteration " + i + " broadcast reply after l2 message");
            assertNull( l1.rcvdRply, "iteration " + i + " l1 reply after l2 message");
            assertNotNull( l2.rcvdRply, "iteration " + i + " l2 reply after l2 message");

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;
            assertEquals( 3 * (i + 1), l.getRcvCount(), "iteration " + i + " l received count ");
        }

    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        LenzCommandStation lcs = new LenzCommandStation();
        tc = new StoppingXNetPacketizer(lcs);
        port = assertDoesNotThrow( () -> new XNetPortControllerScaffold(), "Error creating test port");
    }

    @AfterEach
    @Override
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        port.dispose();
        port = null;
        JUnitUtil.tearDown();
    }

}
