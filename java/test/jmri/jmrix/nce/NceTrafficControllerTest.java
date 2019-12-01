package jmri.jmrix.nce;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the NceTrafficController class
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 */
public class NceTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    @Ignore("Test disabled until threading can be resolved")
    public void testSendAscii() throws Exception {
        NceTrafficController c = new NceTrafficController() {
            // skip timeout message
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        NcePortControllerScaffold p = new NcePortControllerScaffold();
        c.connectPort(p);

        // send a message
        NceMessage m = new NceMessage(3);
        m.setBinary(false);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendNceMessage(m, new NceListenerScaffold());
        Assert.assertEquals("total length ", 4, tostream.available());
        Assert.assertEquals("Char 0", '0', tostream.readByte());
        Assert.assertEquals("Char 1", '1', tostream.readByte());
        Assert.assertEquals("Char 2", '2', tostream.readByte());
        Assert.assertEquals("EOM", 0x0d, tostream.readByte());
        Assert.assertEquals("remaining ", 0, tostream.available());
    }

    @Test
    @Ignore("Test disabled until threading can be resolved")
    public void testSendBinary() throws Exception {
        NceTrafficController c = new NceTrafficController() {
            // skip timeout message
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        NcePortControllerScaffold p = new NcePortControllerScaffold();
        c.connectPort(p);

        // send a message
        NceMessage m = new NceMessage(3);
        m.setBinary(true);
        m.setOpCode(0x81);
        m.setElement(1, 0x12);
        m.setElement(2, 0x34);
        c.sendNceMessage(m, new NceListenerScaffold());
        Assert.assertEquals("total length ", 3, tostream.available());
        Assert.assertEquals("Char 0", 0x81, 0xFF & tostream.readByte());
        Assert.assertEquals("Char 1", 0x12, tostream.readByte());
        Assert.assertEquals("Char 2", 0x34, tostream.readByte());
        Assert.assertEquals("remaining ", 0, tostream.available());
    }

    @Test
    @Ignore("Test disabled until threading can be resolved")
    public void testMonitor() throws Exception {
        NceTrafficController c = new NceTrafficController() {
            // skip timeout message
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        NcePortControllerScaffold p = new NcePortControllerScaffold();
        c.connectPort(p);

        // start monitor
        rcvdMsg = null;
        NceListenerScaffold s = new NceListenerScaffold();
        c.addNceListener(s);

        // send a message
        NceMessage m = new NceMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendNceMessage(m, new NceListenerScaffold());

        // check it arrived at monitor
        Assert.assertEquals("total length ", 4, tostream.available());
        Assert.assertEquals("Char 0", '0', tostream.readByte());
        Assert.assertEquals("Char 1", '1', tostream.readByte());
        Assert.assertEquals("Char 2", '2', tostream.readByte());
        Assert.assertEquals("EOM", 0x0d, tostream.readByte());
        Assert.assertEquals("remaining ", 0, tostream.available());
    }

    @Test
    @Ignore("Test disabled until threading can be resolved")
    public void xtestRcvReply() throws Exception {
        NceTrafficController c = new NceTrafficController() {
            // skip timeout message
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            @Override
            public void receiveLoop() {
            }

            @Override
            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        NcePortControllerScaffold p = new NcePortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        NceListener l = new NceListenerScaffold();
        c.addNceListener(l);

        // send a message
        NceMessage m = new NceMessage(3);
        m.setOpCode('0');
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendNceMessage(m, l);
		// that's already tested, so don't do here.

        // now send reply
        tistream.write('R');
        tistream.write(0x0d);
        tistream.write('C');
        tistream.write('O');
        tistream.write('M');
        tistream.write('M');
        tistream.write('A');
        tistream.write('N');
        tistream.write('D');
        tistream.write(':');
        tistream.write(' ');

        // drive the mechanism
        c.handleOneIncomingReply();
        Assert.assertTrue("reply received ", waitForReply());
        Assert.assertEquals("first char of reply ", 'R', rcvdReply.getOpCode());
    }

    private boolean waitForReply() {
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while (rcvdReply == null && i++ < 100) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("past loop, i=" + i
                    + " reply=" + rcvdReply);
        }
        return i < 100;
    }

    // internal class to simulate a NceListener
    class NceListenerScaffold implements jmri.jmrix.nce.NceListener {

        public NceListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }

        @Override
        public void message(NceMessage m) {
            rcvdMsg = m;
        }

        @Override
        public void reply(NceReply r) {
            rcvdReply = r;
        }
    }
    NceReply rcvdReply;
    NceMessage rcvdMsg;

    // internal class to simulate a NcePortController
    class NcePortControllerScaffold extends NcePortController {

        @Override
        public java.util.Vector<String> getPortNames() {
            return null;
        }

        @Override
        public String openPort(String portName, String appName) {
            return null;
        }

        @Override
        public void configure() {
        }

        @Override
        public String[] validBaudRates() {
            return new String[] {};
        }

        //@Override
        @Override
        public int[] validBaudNumbers() {
            return new int[] {};
        }

        protected NcePortControllerScaffold() throws Exception {
            super(null);
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
        }

        // returns the InputStream from the port
        @Override
        public DataInputStream getInputStream() {
            return istream;
        }

        // returns the outputStream to the port
        @Override
        public DataOutputStream getOutputStream() {
            return ostream;
        }

        // check that this object is ready to operate
        @Override
        public boolean status() {
            return true;
        }
    }
    static DataOutputStream ostream;  // Traffic controller writes to this
    static DataInputStream tostream; // so we can read it from this

    static DataOutputStream tistream; // tests write to this
    static DataInputStream istream;  // so the traffic controller can read from this

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc  = new NceTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


    private final static Logger log = LoggerFactory.getLogger(NceTrafficControllerTest.class);

}
