package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <p>
 * Title: XNetPacketizerTest </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2002</p>
 *
 * @author Bob Jacobsen
 */
public class XNetPacketizerTest extends TestCase {

    /**
     * Local test class to make XNetPacketizer more felicitous to test
     */
    class StoppingXNetPacketizer extends XNetPacketizer {

        public StoppingXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation p) {
            super(p);
        }

        public void stop() {
            xmtThread.stop();
            rcvThread.stop();
        }

        // methods removed for testing
        protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
        } // don't care about timeout

        protected void reportReceiveLoopException(Exception e) {
        }

        protected void portWarn(Exception e) {
        }
    }

    public void testOutbound() throws Exception {
        LenzCommandStation lcs = new LenzCommandStation();
        StoppingXNetPacketizer c = new StoppingXNetPacketizer(lcs);
        // connect to iostream via port controller scaffold
        XNetPortControllerScaffold p = new XNetPortControllerScaffold();
        c.connectPort(p);
        //c.startThreads();
        XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendXNetMessage(m, null);

        p.flush();
        jmri.util.JUnitUtil.waitFor(()->{return p.tostream.available()==4;},"total length 4");

        Assert.assertEquals("total length ", 4, p.tostream.available());
        Assert.assertEquals("Char 0", 0x52, p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 1", 0x05, p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 2", 0x8A, p.tostream.readByte() & 0xff);
        Assert.assertEquals("parity", 0xDD, p.tostream.readByte() & 0xff);
        Assert.assertEquals("remaining ", 0, p.tostream.available());
    }

    public void testInbound() throws Exception {
        LenzCommandStation lcs = new LenzCommandStation();
        StoppingXNetPacketizer c = new StoppingXNetPacketizer(lcs);

        // connect to iostream via port controller
        XNetPortControllerScaffold p = new XNetPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        XNetListenerScaffold l = new XNetListenerScaffold();
        c.addXNetListener(~0, l);

        // now send reply
        p.tistream.write(0x52);
        p.tistream.write(0x12);
        p.tistream.write(0x12);
        p.tistream.write(0x52);

        p.flush();
        jmri.util.JUnitUtil.waitFor(()->{return l.rcvdRply != null;},"reply received");
        
        // check that the message was picked up by the read thread.
        Assert.assertTrue("reply received ", l.rcvdRply != null);
        Assert.assertEquals("first char of reply ", 0x52, l.rcvdRply.getElement(0));
    }

    public void testInterference() throws Exception {
        // This test checks to make sure that when two listeners register for events
        // at the same time, the first listener is still the active listener until
        // it receives a message.
        LenzCommandStation lcs = new LenzCommandStation();
        StoppingXNetPacketizer c = new StoppingXNetPacketizer(lcs);

        // connect to iostream via port controller
        XNetPortControllerScaffold p = new XNetPortControllerScaffold();
        c.connectPort(p);

        // We need three objects to receive messages.
        // The first one recieves broadcast messages. 
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
            p.tistream.write(0x42);
            p.tistream.write(0x12);
            p.tistream.write(0x12);
            p.tistream.write(0x42);

            p.flush();
            jmri.util.JUnitUtil.waitFor(()->{return l.rcvdRply != null;},"reply received");

            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l.rcvdRply != null);
            Assert.assertEquals("iteration " + i + " first char of broadcast reply to l", 0x42, l.rcvdRply.getElement(0));


            // now we need to send a message with both the second and third listeners 
            // as reply receiver.
            XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
            c.sendXNetMessage(m, l1);

            XNetMessage m1 = XNetMessage.getTurnoutCommandMsg(23, true, false, true);
            c.sendXNetMessage(m1, l2);

            p.flush();

            // and now we verify l1 is the last sender.
            jmri.util.JUnitUtil.waitFor(()->{return l1 == c.getLastSender();},"iteration " + i + " Last Sender l1, before l1 reply");

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            // Now we reply to the messages above
            p.tistream.write(0x01);
            p.tistream.write(0x04);
            p.tistream.write(0x05);
            
            p.flush();
            jmri.util.JUnitUtil.waitFor(()->{return l1.rcvdRply != null;},"iteration " + i + " reply received ");
            
            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l1.rcvdRply != null);
            Assert.assertEquals("iteration " + i + " first char of reply to l1", 0x01, l1.rcvdRply.getElement(0));

            Assert.assertNotNull("iteration " + i + " broadcast reply after l1 message",l.rcvdRply);
            Assert.assertNotNull("iteration " + i + " l1 reply after l1 message",l1.rcvdRply);
            Assert.assertNull("iteration " + i + " l2 reply after l1 message",l2.rcvdRply);

            // and now we verify l2 is the last sender.
            jmri.util.JUnitUtil.waitFor(()->{return l2 == c.getLastSender();},"Last Sender l2");

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            p.tistream.write(0x01);
            p.tistream.write(0x04);
            p.tistream.write(0x05);

            p.flush();
            jmri.util.JUnitUtil.waitFor(()->{return l2.rcvdRply != null;},"iteration " + i + " reply received ");

            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", l2.rcvdRply != null);

            Assert.assertEquals("iteration " + i + " first char of reply to l2", 0x01, l2.rcvdRply.getElement(0));
 
            Assert.assertNotNull("iteration " + i + " broadcast reply after l2 message",l.rcvdRply);
            Assert.assertNull("iteration " + i + " l1 reply after l2 message",l1.rcvdRply);
            Assert.assertNotNull("iteration " + i + " l2 reply after l2 message",l2.rcvdRply);

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;
            Assert.assertEquals("iteration " + i + " l received count ", 3 * (i + 1), l.rcvCount);
        }

    }

    public XNetPacketizerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetPacketizerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
