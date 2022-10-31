package jmri.jmrix.tmcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the SerialTrafficController class.
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class SerialTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    public void testAddListener() {
        SerialTrafficController _tc = (SerialTrafficController) tc;
        SerialListenerScaffold sls = new SerialListenerScaffold();

        _tc.addSerialListener(sls);
    }

    @Test
    @Disabled("this test is disabled until the threading can be worked out")
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

        Assert.assertTrue( m == rcvdMsg );
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

            @Override
            protected void unexpectedReplyStateError(int State, String msgString) {
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
        JUnitUtil.waitFor(100);

        // drive the mechanism
        c.handleOneIncomingReply();
        JUnitUtil.waitFor(() -> {
            return rcvdReply != null;
        }, "reply received");
        Assert.assertEquals("first char of reply ", 0xFE, rcvdReply.getOpCode() & 0xFF);
        Assert.assertEquals("length of reply ", 3, rcvdReply.getNumDataElements());
        c.terminateThreads();
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

            @Override
            protected void unexpectedReplyStateError(int State, String msgString) {
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
        JUnitUtil.waitFor(100);

        // drive the mechanism
        c.handleOneIncomingReply();
        JUnitUtil.waitFor(() -> {
            return rcvdReply != null;
        }, "reply received");
        Assert.assertEquals("first char of reply ", 0xF0, rcvdReply.getOpCode() & 0xFF);
        Assert.assertEquals("length of reply ", 1, rcvdReply.getNumDataElements());
        jmri.util.JUnitAppender.assertWarnMessage("return short message as 1st byte is 240");
        c.terminateThreads();
    }

    // internal class to simulate a Listener
    private class SerialListenerScaffold implements jmri.jmrix.tmcc.SerialListener {

        protected SerialListenerScaffold() {
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
    private class SerialPortControllerScaffold extends SerialPortController {

        SerialPortControllerScaffold() throws java.io.IOException {
            super(scm);
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
        }

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

    private DataOutputStream ostream;  // Traffic controller writes to this
    private DataInputStream tostream;  // so we can read it from this

    private DataOutputStream tistream; // Tests write to this
    private DataInputStream istream;   // so the traffic controller can read from this

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        scm = new TmccSystemConnectionMemo("T", "TMCC Test"); // use a common memo to prevent T2, T3 unconnected instances
        tc = new SerialTrafficController(scm); // TrafficController for tests in super (AbstractMRTrafficControllerTest)
        c = null;
    }

    SerialTrafficController c; // TrafficController for tests in this class
    TmccSystemConnectionMemo scm = null;
    
    @AfterEach
    @Override
    public void tearDown() {
        if (c != null) { 
            c.terminateThreads();
        }
        scm.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialTrafficControllerTest.class);

}
