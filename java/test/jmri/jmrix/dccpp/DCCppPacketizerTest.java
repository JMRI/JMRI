package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    class StoppingDCCppPacketizer extends DCCppPacketizer {

        public StoppingDCCppPacketizer(jmri.jmrix.dccpp.DCCppCommandStation p) {
            super(p);
        }

        public void stop() {
            xmtThread.stop();
            rcvThread.stop();
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
        DCCppPacketizer c = (DCCppPacketizer)tc;
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

    @Test
    public void testInbound() throws Exception {
        DCCppPacketizer c = (DCCppPacketizer)tc;

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

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        DCCppCommandStation lcs = new DCCppCommandStation();
        tc = new StoppingDCCppPacketizer(lcs);
    }

    @After
    @Override
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppPacketizerTest.class);

}
