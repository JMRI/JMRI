package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    class StoppingXNetPacketizer extends XNetPacketizer {

        public StoppingXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation p) {
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
    public void testOutbound() throws Exception {
        XNetPacketizer c = (XNetPacketizer) tc;
        // connect to iostream via port controller scaffold
        tc.connectPort(port);
        XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendXNetMessage(m, null);

        port.flush();
        jmri.util.JUnitUtil.waitFor(() -> {
            return port.tostream.available() == 4;
        }, "total length 4");

        Assert.assertEquals("total length ", 4, port.tostream.available());
        Assert.assertEquals("Char 0", 0x52, port.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 1", 0x05, port.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 2", 0x8A, port.tostream.readByte() & 0xff);
        Assert.assertEquals("parity", 0xDD, port.tostream.readByte() & 0xff);
        Assert.assertEquals("remaining ", 0, port.tostream.available());
    }

    @Test
    public void testInbound() throws Exception {
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
        jmri.util.JUnitUtil.waitFor(() -> {
            return l.rcvdRply != null;
        }, "reply received");

        // check that the message was picked up by the read thread.
        Assert.assertTrue("reply received ", l.rcvdRply != null);
        Assert.assertEquals("first char of reply ", 0x52, l.rcvdRply.getElement(0));
    }

    @Test
    public void testInterference() throws Exception {
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
            jmri.util.JUnitUtil.waitFor(() -> {
                return l.rcvdRply != null;
            }, "reply received");

            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l.rcvdRply != null);
            Assert.assertEquals("iteration " + i + " first char of broadcast reply to l", 0x42, l.rcvdRply.getElement(0));

            // now we need to send a message with both the second and third listeners
            // as reply receiver.
            XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
            c.sendXNetMessage(m, l1);

            XNetMessage m1 = XNetMessage.getTurnoutCommandMsg(23, true, false, true);
            c.sendXNetMessage(m1, l2);

            port.flush();

            // and now we verify l1 is the last sender.
            jmri.util.JUnitUtil.waitFor(() -> {
                return l1 == c.getLastSender();
            }, "iteration " + i + " Last Sender l1, before l1 reply");

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            // Now we reply to the messages above
            port.tistream.write(0x01);
            port.tistream.write(0x04);
            port.tistream.write(0x05);

            port.flush();
            jmri.util.JUnitUtil.waitFor(() -> {
                return l1.rcvdRply != null;
            }, "iteration " + i + " reply received ");

            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l1.rcvdRply != null);
            Assert.assertEquals("iteration " + i + " first char of reply to l1", 0x01, l1.rcvdRply.getElement(0));

            Assert.assertNotNull("iteration " + i + " broadcast reply after l1 message", l.rcvdRply);
            Assert.assertNotNull("iteration " + i + " l1 reply after l1 message", l1.rcvdRply);
            Assert.assertNull("iteration " + i + " l2 reply after l1 message", l2.rcvdRply);

            // and now we verify l2 is the last sender.
            jmri.util.JUnitUtil.waitFor(() -> {
                return l2 == c.getLastSender();
            }, "Last Sender l2");

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            port.tistream.write(0x01);
            port.tistream.write(0x04);
            port.tistream.write(0x05);

            port.flush();
            jmri.util.JUnitUtil.waitFor(() -> {
                return l2.rcvdRply != null;
            }, "iteration " + i + " reply received ");

            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l2.rcvdRply != null);

            Assert.assertEquals("iteration " + i + " first char of reply to l2", 0x01, l2.rcvdRply.getElement(0));

            Assert.assertNotNull("iteration " + i + " broadcast reply after l2 message", l.rcvdRply);
            Assert.assertNull("iteration " + i + " l1 reply after l2 message", l1.rcvdRply);
            Assert.assertNotNull("iteration " + i + " l2 reply after l2 message", l2.rcvdRply);

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;
            Assert.assertEquals("iteration " + i + " l received count ", 3 * (i + 1), l.rcvCount);
        }

    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        LenzCommandStation lcs = new LenzCommandStation();
        tc = new StoppingXNetPacketizer(lcs);
        try {
           port = new XNetPortControllerScaffold();
        } catch (Exception e) {
           Assert.fail("Error creating test port");
        }
    }

    @After
    @Override
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
