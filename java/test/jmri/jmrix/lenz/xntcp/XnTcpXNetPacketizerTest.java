package jmri.jmrix.lenz.xntcp;

import jmri.jmrix.lenz.XNetPortControllerScaffold;
import jmri.jmrix.lenz.XNetListenerScaffold;
import jmri.jmrix.lenz.XNetMessage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Title: XnTcpXNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class XnTcpXNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    @Override
    @Test
    public void testInbound() throws Exception {
        XnTcpXNetPacketizer c = (XnTcpXNetPacketizer)tc;

        // connect to iostream via port controller
        XnTcpPortControllerScaffold p = new XnTcpPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        XNetListenerScaffold l = new XNetListenerScaffold();
        c.addXNetListener(~0, l);

        // now send reply
        p.tistream.write(0x52);
        p.tistream.write(0x12);
        p.tistream.write(0x12);
        p.tistream.write(0x52);

        p.tistream.flush();
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdRply() != null;},"reply received");
        // check that the message was picked up by the read thread.
        Assert.assertTrue("reply received ", l.getRcvdRply() != null);
        Assert.assertEquals("first char of reply ", 0x52, l.getRcvdRply().getElement(0));
    }

    @Override
    @Test
    public void testInterference() throws Exception {
        // This test checks to make sure that when two listeners register for events
        // at the same time, the first listener is still the active listener until
        // it receives a message.
        XnTcpXNetPacketizer c = (XnTcpXNetPacketizer)tc;

        // connect to iostream via port controller
        XnTcpPortControllerScaffold p = new XnTcpPortControllerScaffold();
        c.connectPort(p);

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

            l.setRcvdRply(null);
            l1.setRcvdRply(null);
            l2.setRcvdRply(null);

            // first, we send an unsolicited message
            p.tistream.write(0x42);
            p.tistream.write(0x12);
            p.tistream.write(0x12);
            p.tistream.write(0x42);

            p.flush();
            jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdRply() != null;},"reply received");

            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l.getRcvdRply() != null);
            Assert.assertEquals("iteration " + i + " first char of broadcast reply to l", 0x42, l.getRcvdRply().getElement(0));
            // now we need to send a message with both the second and third listeners
            // as reply receiver.
            XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
            c.sendXNetMessage(m, l1);

            XNetMessage m1 = XNetMessage.getTurnoutCommandMsg(23, true, false, true);
            c.sendXNetMessage(m1, l2);

            p.flush();

            // and now we verify l1 is the last sender.
            jmri.util.JUnitUtil.waitFor(()->{return l1 == c.getLastSender();},"iteration " + i + " Last Sender l1, before l1 reply");

            l.setRcvdRply(null);
            l1.setRcvdRply(null);
            l2.setRcvdRply(null);

            // Now we reply to the messages above
            p.tistream.write(0x01);
            p.tistream.write(0x04);
            p.tistream.write(0x05);

            p.flush();
            jmri.util.JUnitUtil.waitFor(()->{return l1.getRcvdRply() != null;},"iteration " + i + " reply received ");

            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l1.getRcvdRply() != null);
            Assert.assertEquals("iteration " + i + " first char of reply to l1", 0x01, l1.getRcvdRply().getElement(0));

            Assert.assertNotNull("iteration " + i + " broadcast reply after l1 message",l.getRcvdRply());
            Assert.assertNotNull("iteration " + i + " l1 reply after l1 message",l1.getRcvdRply());
            Assert.assertNull("iteration " + i + " l2 reply after l1 message",l2.getRcvdRply());

            // and now we verify l2 is the last sender.
            jmri.util.JUnitUtil.waitFor(()->{return l2 == c.getLastSender();},"Last Sender l2");

            l.setRcvdRply(null);
            l1.setRcvdRply(null);
            l2.setRcvdRply(null);

            p.tistream.write(0x01);
            p.tistream.write(0x04);
            p.tistream.write(0x05);

            p.flush();
            jmri.util.JUnitUtil.waitFor(()->{return l2.getRcvdRply() != null;},"iteration " + i + " reply received ");

            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l2.getRcvdRply() != null);

            Assert.assertEquals("iteration " + i + " first char of reply to l2", 0x01, l2.getRcvdRply().getElement(0));
            Assert.assertNotNull("iteration " + i + " broadcast reply after l2 message",l.getRcvdRply());
            Assert.assertNull("iteration " + i + " l1 reply after l2 message",l1.getRcvdRply());
            Assert.assertNotNull("iteration " + i + " l2 reply after l2 message",l2.getRcvdRply());

            l.setRcvdRply(null);
            l1.setRcvdRply(null);
            l2.setRcvdRply(null);
            Assert.assertEquals("iteration " + i + " l received count ", 3 * (i + 1), l.getRcvCount());
        }

    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XnTcpXNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
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
        jmri.util.JUnitUtil.tearDown();
    }

}
