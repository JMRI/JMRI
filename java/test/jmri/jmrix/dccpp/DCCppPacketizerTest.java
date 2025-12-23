package jmri.jmrix.dccpp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * <p>
 * Title: DCCppPacketizerTest </p>
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Mark Underwood Copyright(C) 2015
 */
public class DCCppPacketizerTest extends DCCppTrafficControllerTest {

    /**
     * Local test class to make DCCppPacketizer more felicitous to test
     */
    private static class StoppingDCCppPacketizer extends DCCppPacketizer {

        StoppingDCCppPacketizer(jmri.jmrix.dccpp.DCCppCommandStation p) {
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
        DCCppPacketizer c = (DCCppPacketizer) tc;
        // connect to iostream via port controller scaffold
        DCCppPortControllerScaffold p = new DCCppPortControllerScaffold();
        c.connectPort(p);
        //c.startThreads();
        DCCppMessage m = DCCppMessage.makeTurnoutCommandMsg(22, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendDCCppMessage(m, null);
        log.debug("Message = {} length = {}", m.toString(), m.getNumDataElements());
        JUnitUtil.waitFor(JUnitUtil.WAITFOR_DEFAULT_DELAY); // Allow time for other threads to send 4 characters
        //assertEquals("total length ", 8, p.tostream.available());
        assertEquals( '<', p.tostream.readByte() & 0xff, "Char 0");
        assertEquals( 'T', p.tostream.readByte() & 0xff, "Char 1");
        assertEquals( ' ', p.tostream.readByte() & 0xff, "Char 2");
        assertEquals( '2', p.tostream.readByte() & 0xff, "Char 3");
        assertEquals( '2', p.tostream.readByte() & 0xff, "Char 4");
        assertEquals( ' ', p.tostream.readByte() & 0xff, "Char 5");
        assertEquals( '1', p.tostream.readByte() & 0xff, "Char 6");
        assertEquals( '>', p.tostream.readByte() & 0xff, "Char 7");
        assertEquals( 0, p.tostream.available(), "remaining ");
    }

    @Test
    public void testInbound() throws IOException {
        DCCppPacketizer c = (DCCppPacketizer) tc;

        log.debug("Running testInbound() test");

        // connect to iostream via port controller
        DCCppPortControllerScaffold p = new DCCppPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        DCCppListenerScaffold l = new DCCppListenerScaffold();
        c.addDCCppListener(~0, l);

        // now send reply
        // NOTE: The PortControllerScaffold doesn't model the real PortController, which will
        // pre-strip the < > characters out of the stream before forwarding it.
        // So we don't include them here.
        p.tistream.write('<');
        p.tistream.write('H');
        p.tistream.write(' ');
        p.tistream.write('2');
        p.tistream.write('2');
        p.tistream.write(' ');
        p.tistream.write('1');
        p.tistream.write('>');

        // check that the message was picked up by the read thread.
        assertTrue( waitForReply(l), "reply received");
        log.debug("Reply string = {} length = {}", l.rcvdRply.toString(), l.rcvdRply.getNumDataElements());
        assertEquals( 'H', l.rcvdRply.getElement(0) & 0xFF, "Char 0 ");
        assertEquals( ' ', l.rcvdRply.getElement(1) & 0xFF, "Char 1 ");
        assertEquals( '2', l.rcvdRply.getElement(2) & 0xFF, "Char 2 ");
        assertEquals( '2', l.rcvdRply.getElement(3) & 0xFF, "Char 3 ");
        assertEquals( ' ', l.rcvdRply.getElement(4) & 0xFF, "Char 4 ");
        assertEquals( '1', l.rcvdRply.getElement(5) & 0xFF, "Char 5 ");
    }

    private boolean waitForReply(DCCppListenerScaffold l) {
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while (l.rcvdRply == null && i++ < 100) {
            JUnitUtil.waitFor(JUnitUtil.WAITFOR_DEFAULT_DELAY);
        }
        if (log.isDebugEnabled()) {
            log.debug("past loop, i={} reply={}", i, l.rcvdRply);
        }
        if (i == 0) {
            log.warn("waitForReply saw an immediate return; is threading right?");
        }
        return i < 100;
    }

    @Test
    @Override
    public void testPortReadyToSendNullController() {
        super.testPortReadyToSendNullController();
        JUnitAppender.suppressWarnMessageStartsWith("DCC++ port not ready to send");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        DCCppCommandStation lcs = new DCCppCommandStation();
        tc = new StoppingDCCppPacketizer(lcs);
    }

    @AfterEach
    @Override
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppPacketizerTest.class);

}
