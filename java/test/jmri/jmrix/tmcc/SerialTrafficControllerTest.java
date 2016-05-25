package jmri.jmrix.tmcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the SerialTrafficController class
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class SerialTrafficControllerTest extends TestCase {

    public void testCreate() {
        SerialTrafficController m = new SerialTrafficController();
        Assert.assertNotNull("exists", m);
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

    public void testAddListener() {
        SerialTrafficController m = new SerialTrafficController();
        SerialListenerScaffold c = new SerialListenerScaffold();

        m.addSerialListener(c);
    }

    /**
     * this test is disabled until the threading can be worked out
     */
    public void xtestSendOK() throws Exception {
        SerialTrafficController c = new SerialTrafficController() {
            // skip timeout message
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            public void receiveLoop() {
            }

            protected void portWarn(Exception e) {
            }
        };

        // connect to iostream via port controller
        SerialPortControllerScaffold p = new SerialPortControllerScaffold();
        c.connectPort(p);

        // send a message
        SerialMessage m = new SerialMessage();
        m.setOpCode(0xFE);
        m.setElement(1, 0x21);
        m.setElement(2, 0x44);
        c.sendSerialMessage(m, new SerialListenerScaffold());
        Assert.assertEquals("total length ", 3, tostream.available());
        Assert.assertEquals("Byte 0", 0xFE, 0xFF & tostream.readByte());
        Assert.assertEquals("Byte 1", 0x21, 0xFF & tostream.readByte());
        Assert.assertEquals("Byte 2", 0x44, 0xFF & tostream.readByte());
        Assert.assertEquals("remaining ", 0, tostream.available());
    }

    public void testRcvReplyOK() throws Exception {
        SerialTrafficController c = new SerialTrafficController() {
            // skip timeout message
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            public void receiveLoop() {
            }

            protected void portWarn(Exception e) {
            }
        };

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
            // that's already tested, so don't do here.

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

    public void testRcvReplyShort() throws Exception {
        SerialTrafficController c = new SerialTrafficController() {
            // skip timeout message
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }

            public void receiveLoop() {
            }

            protected void portWarn(Exception e) {
            }
        };

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
            // that's already tested, so don't do here.

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

        public void message(SerialMessage m) {
            rcvdMsg = m;
        }

        public void reply(SerialReply r) {
            rcvdReply = r;
        }
    }
    SerialReply rcvdReply;
    SerialMessage rcvdMsg;

    // internal class to simulate a PortController
    class SerialPortControllerScaffold extends SerialPortController {

        public java.util.Vector<String> getPortNames() {
            return null;
        }

        public String openPort(String portName, String appName) {
            return null;
        }

        public void configure() {
        }

        public String[] validBaudRates() {
            return null;
        }

        protected SerialPortControllerScaffold() throws Exception {
            super(new TMCCSystemConnectionMemo());
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            istream = new DataInputStream(tempPipe);
            tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
        }

        // returns the InputStream from the port
        public DataInputStream getInputStream() {
            return istream;
        }

        // returns the outputStream to the port
        public DataOutputStream getOutputStream() {
            return ostream;
        }

        // check that this object is ready to operate
        public boolean status() {
            return true;
        }
    }
    static DataOutputStream ostream;  // Traffic controller writes to this
    static DataInputStream tostream; // so we can read it from this

    static DataOutputStream tistream; // tests write to this
    static DataInputStream istream;  // so the traffic controller can read from this

    // from here down is testing infrastructure
    public SerialTrafficControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialTrafficControllerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialTrafficControllerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficControllerTest.class.getName());

}
