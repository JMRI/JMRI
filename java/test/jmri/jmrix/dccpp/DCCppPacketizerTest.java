package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Title: DCCppPacketizerTest </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2002, 2015</p>
 *
 * @author Bob Jacobsen
 * @author Mark Underwood
 * @version $Revision$
 */
public class DCCppPacketizerTest extends TestCase {

    /**
     * Local test class to make DCCppPacketizer more felicitous to test
     */
    class StoppingDCCppPacketizer extends DCCppPacketizer {

        public StoppingDCCppPacketizer(jmri.jmrix.dccpp.DCCppCommandStation p) {
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
        DCCppCommandStation lcs = new DCCppCommandStation();
        StoppingDCCppPacketizer c = new StoppingDCCppPacketizer(lcs);
        // connect to iostream via port controller scaffold
        DCCppPortControllerScaffold p = new DCCppPortControllerScaffold();
        c.connectPort(p);
        //c.startThreads();
        DCCppMessage m = DCCppMessage.makeTurnoutCommandMsg(22, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendDCCppMessage(m, null);
	log.debug("Message = {} length = {}", m.toString(), m.getNumDataElements());
        jmri.util.JUnitUtil.releaseThread(this); // Allow time for other threads to send 4 characters
        //Assert.assertEquals("total length ", 8, p.tostream.available());
        Assert.assertEquals("Char 0",'<', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 1",'T', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 2", ' ', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 3", '2', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 4", '2', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 5", ' ', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 6", '1', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 7", '>', p.tostream.readByte() & 0xff);
        Assert.assertEquals("remaining ", 0, p.tostream.available());
    }

    public void testInbound() throws Exception {
        DCCppCommandStation lcs = new DCCppCommandStation();
        StoppingDCCppPacketizer c = new StoppingDCCppPacketizer(lcs);

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
        Assert.assertTrue("reply received ", waitForReply(l));
	log.debug("Reply string = {} length = {}",l.rcvdRply.toString(), l.rcvdRply.getNumDataElements());
        Assert.assertEquals("Char 0 ", 'H', l.rcvdRply.getElement(0) & 0xFF);
        Assert.assertEquals("Char 1 ", ' ', l.rcvdRply.getElement(1) & 0xFF);
        Assert.assertEquals("Char 2 ", '2', l.rcvdRply.getElement(2) & 0xFF);
        Assert.assertEquals("Char 3 ", '2', l.rcvdRply.getElement(3) & 0xFF);
        Assert.assertEquals("Char 4 ", ' ', l.rcvdRply.getElement(4) & 0xFF);
        Assert.assertEquals("Char 5 ", '1', l.rcvdRply.getElement(5) & 0xFF);
    }
    /*
    public void testInterference() throws Exception {
        // This test checks to make sure that when two listeners register for events
        // at the same time, the first listener is still the active listener until
        // it receives a message.
        DCCppCommandStation lcs = new DCCppCommandStation();
        StoppingDCCppPacketizer c = new StoppingDCCppPacketizer(lcs);

        // connect to iostream via port controller
        DCCppPortControllerScaffold p = new DCCppPortControllerScaffold();
        c.connectPort(p);

        // We need three objects to receive messages.
        // The first one recieves broadcast messages. 
        // The others only receive directed messages.
        DCCppListenerScaffold l = new DCCppListenerScaffold();
        DCCppListenerScaffold l1 = new DCCppListenerScaffold();
        DCCppListenerScaffold l2 = new DCCppListenerScaffold();
        c.addDCCppListener(~0, l);

        // we're going to loop through this, because we're trying to catch
        // a threading/synchronization issue in AbstractMRTrafficController.
        for (int i = 0; i < 5; i++) {

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            // first, we send an unsolicited message
	    p.tistream.write('<');
	    p.tistream.write('H');
	    p.tistream.write(' ');
	    p.tistream.write('1');
	    p.tistream.write('2');
	    p.tistream.write(' ');
	    p.tistream.write('1');
	    p.tistream.write('>');


            // check that the message was picked up by the read thread.
            Assert.assertTrue("iteration " + i + " reply received ", waitForReply(l));
            Assert.assertEquals("iteration " + i + " first char of broadcast reply to l", 'H', l.rcvdRply.getElement(0));


            // now we need to send a message with both the second and third listeners 
            // as reply receiver.
            DCCppMessage m = DCCppMessage.makeTurnoutCommandMsg(22, true);
            c.sendDCCppMessage(m, l1);

            DCCppMessage m1 = DCCppMessage.makeTurnoutCommandMsg(23, true);
            c.sendDCCppMessage(m1, l2);

            jmri.util.JUnitUtil.releaseThread(this); // Allow time for messages to process into the system

            // and now we verify l1 is the last sender.
            //Assert.assertEquals("itteration " + i + " Last Sender l1, before l1 reply", l1, c.getLastSender());

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

            // Now we reply to the messages above
	    p.tistream.write('<');
	    p.tistream.write('K'); // Nonsense character for testing purposes.
	    p.tistream.write(' ');
	    p.tistream.write('2');
	    p.tistream.write('2');
	    p.tistream.write(' ');
	    p.tistream.write('1');
	    p.tistream.write('>');

            // check that the message was picked up by the read thread.
            Assert.assertTrue("itteration " + i + " reply received ", waitForReply(l1));
            Assert.assertEquals("itteration " + i + " first char of reply to l1", 'K', l1.rcvdRply.getElement(0));

            Assert.assertNotNull("itteration " + i + " broadcast reply after l1 message",l.rcvdRply);
            Assert.assertNotNull("itteration " + i + " l1 reply after l1 message",l1.rcvdRply);
            Assert.assertNull("itteration " + i + " l2 reply after l1 message",l2.rcvdRply);

            jmri.util.JUnitUtil.releaseThread(this); // Allow time for messages to process into the system

            // and now we verify l2 is the last sender.
            Assert.assertEquals("Last Sender l2", l2, c.getLastSender());
            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;

	    p.tistream.write('<');
	    p.tistream.write('J'); // Nonsense character for testing purposes.
	    p.tistream.write(' ');
	    p.tistream.write('2');
	    p.tistream.write('3');
	    p.tistream.write(' ');
	    p.tistream.write('1');
	    p.tistream.write('>');

            // check that the message was picked up by the read thread.
            Assert.assertTrue("itteration " + i + " reply received ", waitForReply(l2));

            Assert.assertEquals("itteration " + i + " first char of reply to l2", 'J', l2.rcvdRply.getElement(0));
 
            Assert.assertNotNull("itteration " + i + " broadcast reply after l2 message",l.rcvdRply);
            Assert.assertNull("itteration " + i + " l1 reply after l2 message",l1.rcvdRply);
            Assert.assertNotNull("itteration " + i + " l2 reply after l2 message",l2.rcvdRply);

            l.rcvdRply = null;
            l1.rcvdRply = null;
            l2.rcvdRply = null;
            Assert.assertEquals("itteration " + i + " l received count ", 3 * (i + 1), l.rcvCount);
        }

    }
*/
    private boolean waitForReply(DCCppListenerScaffold l) {
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while (l.rcvdRply == null && i++ < 100) {
            jmri.util.JUnitUtil.releaseThread(this);
        }
        if (log.isDebugEnabled()) {
            log.debug("past loop, i=" + i
                    + " reply=" + l.rcvdRply);
        }
        if (i == 0) {
            log.warn("waitForReply saw an immediate return; is threading right?");
        }
        return i < 100;
    }

    public DCCppPacketizerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppPacketizerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppPacketizerTest.class.getName());

}
