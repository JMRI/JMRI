package jmri.jmrix.tmcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the SerialTrafficController class.
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class SerialTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    private boolean waitForReply() {
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while (rcvdReply == null && i++ < 100) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        log.debug("past loop, i={} reply={}", i, rcvdReply);
        return i < 100;
    }

    @Test
    public void testAddListener() {
        SerialTrafficController _tc = (SerialTrafficController) tc;
        SerialListenerScaffold sls = new SerialListenerScaffold();

        _tc.addSerialListener(sls);
    }

    @Test
    @Ignore("this test is disabled until the threading can be worked out")
    public void testSendOK() throws Exception {
        c = new SerialTrafficController(scm) {
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
        scm.setTrafficController(c);

        // connect to iostream via port controller
        SerialPortControllerScaffold p = new SerialPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        SerialListener l = new SerialListenerScaffold();
        c.addSerialListener(l);

        // send a message
        SerialMessage m = new SerialMessage();
        m.setOpCode(0xFE);
        m.setElement(1, 0x21);
        m.setElement(2, 0x44);
        c.sendSerialMessage(m, l);

        Assert.assertEquals("total length ", 3, tostream.available());
        Assert.assertEquals("Byte 0", 0xFE, 0xFF & tostream.readByte());
        Assert.assertEquals("Byte 1", 0x21, 0xFF & tostream.readByte());
        Assert.assertEquals("Byte 2", 0x44, 0xFF & tostream.readByte());
        Assert.assertEquals("remaining ", 0, tostream.available());
    }

    @Test
    public void testRcvReplyOK() throws Exception {
        c = new SerialTrafficController(scm) {
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
        scm.setTrafficController(c);

        // connect to iostream via port controller
        SerialPortControllerScaffold p = new SerialPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        SerialListener l = new SerialListenerScaffold();
        c.addSerialListener(l);

        // send a message
        SerialMessage m = new SerialMessage();
        m.setOpCode(0xFE);
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendSerialMessage(m, l);
            // that's already tested, so don't do it here.

        // now send reply
        tistream.write(0xFE);
        tistream.write(0x0d);
        tistream.write(0x02);

        // wait for state transition
        synchronized (this) {
            wait(100);
        }

        // drive the mechanism
        c.handleOneIncomingReply();
        Assert.assertTrue("reply received ", waitForReply());
        Assert.assertEquals("first char of reply ", 0xFE, rcvdReply.getOpCode() & 0xFF);
        Assert.assertEquals("length of reply ", 3, rcvdReply.getNumDataElements());
    }

    @Test
    public void testRcvReplyShort() throws Exception {
        c = new SerialTrafficController(scm) {
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
        scm.setTrafficController(c);

        // connect to iostream via port controller
        SerialPortControllerScaffold p = new SerialPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        SerialListener l = new SerialListenerScaffold();
        c.addSerialListener(l);

        // send a message
        SerialMessage m = new SerialMessage();
        m.setOpCode(0xFE);
        m.setElement(1, '1');
        m.setElement(2, '2');
        c.sendSerialMessage(m, l);
            // that's already tested, so don't do it here.

        // now send reply
        tistream.write(0xF0);

        // wait for state transition
        synchronized (this) {
            wait(100);
        }

        // drive the mechanism
        c.handleOneIncomingReply();
        Assert.assertTrue("reply received ", waitForReply());
        Assert.assertEquals("first char of reply ", 0xF0, rcvdReply.getOpCode() & 0xFF);
        Assert.assertEquals("length of reply ", 1, rcvdReply.getNumDataElements());
        jmri.util.JUnitAppender.assertWarnMessage("return short message as 1st byte is 240");
    }

    // internal class to simulate a Listener
    class SerialListenerScaffold implements jmri.jmrix.tmcc.SerialListener {

        public SerialListenerScaffold() {
            rcvdReply = null;
            rcvdMsg = null;
        }

        @Override
        public void message(SerialMessage m) {
            rcvdMsg = m;
        }

        @Override
        public void reply(SerialReply r) {
            rcvdReply = r;
        }
    }
    SerialReply rcvdReply;
    SerialMessage rcvdMsg;

    // internal class to simulate a PortController
    class SerialPortControllerScaffold extends SerialPortController {

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

        protected SerialPortControllerScaffold() throws Exception {
            super(scm);
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
    static DataInputStream tostream;  // so we can read it from this

    static DataOutputStream tistream; // Tests write to this
    static DataInputStream istream;   // so the traffic controller can read from this

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        scm = new TmccSystemConnectionMemo("T", "TMCC Test"); // use a common memo to prevent T2, T3 unconnected instances
        tc = new SerialTrafficController(scm); // TrafficController for tests in super (AbstractMRTrafficControllerTest)
        c = null;
    }

    SerialTrafficController c; // TrafficController for tests in this class
    TmccSystemConnectionMemo scm = null;
    
    @After
    @Override
    public void tearDown() {
        if (c != null) c.terminateThreads();
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficControllerTest.class);

}
